package skadistats.clarity.analyzer.replay;

import javafx.application.Platform;
import javafx.collections.ObservableListBase;
import skadistats.clarity.model.EngineType;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityDeleted;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.entities.OnEntityUpdatesCompleted;
import skadistats.clarity.processor.reader.OnReset;
import skadistats.clarity.processor.reader.ResetPhase;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.wire.common.proto.Demo;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ObservableEntityList extends ObservableListBase<ObservableEntity> {

    private static final int CF_LIST = 0x01;
    private static final int CF_PROP = 0x02;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition platformUpToDate = lock.newCondition();

    private int changeFlag = 0;
    private boolean resetInProgress = false;
    private boolean syncRequested = false;

    private ObservableEntity[] entities;
    private ObservableEntity[] changes;

    public ObservableEntityList(EngineType engineType) {
        entities = new ObservableEntity[1 << engineType.getIndexBits()];
        changes = new ObservableEntity[1 << engineType.getIndexBits()];
        for (int i = 0; i < entities.length; i++) {
            entities[i] = new ObservableEntity(i);
        }
    }

    @Override
    public ObservableEntity get(int index) {
        return entities[index];
    }

    @Override
    public int size() {
        return entities.length;
    }

    private void commitToPlatform() {
        lock.lock();
        try {
            if ((changeFlag & CF_LIST) != 0) {
                beginChange();
                for (int i = 0; i < changes.length; i++) {
                    if (changes[i] != null) {
                        ObservableEntity old = entities[i];
                        entities[i] = changes[i];
                        changes[i] = null;
                        nextSet(i, old);
                    }
                }
                endChange();
                changeFlag &= ~CF_LIST;
            }
            if ((changeFlag & CF_PROP) != 0) {
                for (int i = 0; i < entities.length; i++) {
                    if (entities[i] != null) {
                        entities[i].commitChange();
                    }
                }
                changeFlag &= ~CF_PROP;
            }
            syncRequested = false;
            platformUpToDate.signal();
        } finally {
            lock.unlock();
        }
    }

    private void markChange(int type) {
        if ((type & CF_LIST) != 0 && (changeFlag & CF_LIST) == 0) {
            changeFlag |= CF_LIST;
        }
        if ((type & CF_PROP) != 0 && (changeFlag & CF_PROP) == 0) {
            changeFlag |= CF_PROP;
        }
    }

    private void requestSync() {
        if (resetInProgress) {
            return;
        }
        if (!syncRequested && changeFlag != 0) {
            syncRequested = true;
            Platform.runLater(this::commitToPlatform);
        }
    }

    @OnReset
    public void onReset(Context ctx, Demo.CDemoStringTables packet, ResetPhase phase) {
        lock.lock();
        try {
            if (phase == ResetPhase.START) {
                if (syncRequested) {
                    platformUpToDate.awaitUninterruptibly();
                }
                resetInProgress = true;
            } else if (phase == ResetPhase.CLEAR) {
                markChange(CF_LIST);
                for (int i = 0; i < changes.length; i++) {
                    changes[i] = new ObservableEntity(i);
                }
            } else if (phase == ResetPhase.COMPLETE) {
                resetInProgress = false;
                requestSync();
            }
        } finally {
            lock.unlock();
        }
    }

    @OnEntityCreated
    public void onCreate(Context ctx, Entity entity) {
        lock.lock();
        try {
            markChange(CF_LIST);
            int i = entity.getIndex();
            //System.out.println("create " + i);
            changes[i] = new ObservableEntity(entity);
        } finally {
            lock.unlock();
        }
    }

    @OnEntityUpdated
    public void onUpdate(Context ctx, Entity entity, FieldPath[] fieldPaths, int num) {
        lock.lock();
        try {
            markChange(CF_PROP);
            int i = entity.getIndex();
            ObservableEntity e = changes[i] == null ? entities[i] : changes[i];
            e.update(fieldPaths, num);
        } finally {
            lock.unlock();
        }
    }

    @OnEntityDeleted
    public void onDelete(Context ctx, Entity entity) {
        lock.lock();
        try {
            markChange(CF_LIST);
            int i = entity.getIndex();
            //System.out.println("delete " + i);
            changes[i] = new ObservableEntity(i);
        } finally {
            lock.unlock();
        }
    }

    @OnEntityUpdatesCompleted
    public void onUpdatesCompleted(Context ctx) {
        lock.lock();
        try {
            requestSync();
        } finally {
            lock.unlock();
        }
    }

}
