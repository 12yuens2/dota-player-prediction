const rp = require("request-promise-native");
const request = require("request");
const fs = require('fs');
const util = require('util');

const account_id = process.argv[2];
const hero_id = process.argv[3];

//const account_id = 255077828; //https://www.opendota.com/players/255077828
//const hero_id = 74; //invoker

const requestMatches = (account_id, limit=50, game_mode=3, hero_id) => {
    const options = {
        uri: `https://api.opendota.com/api/players/${account_id}/matches`,
        qs: {
            "limit": limit,
            "game_mode": game_mode
        },
        json: true
    };

    if (!hero_id) {
        options.qs.hero_id = hero_id;
    }

    return rp(options);
}

const requestReplay = (match_id) => {
    const options = {
        uri: `https://api.opendota.com/api/replays`,
        qs: {
            'match_id': match_id
        },
        json: true
    };

    return rp(options);
}

const downloadReplay = (details) => {
    const cluster = details.cluster;
    const match_id = details.match_id;
    const replay_salt = details.replay_salt;

    const options = {
        uri: `http://replay${cluster}.valve.net/570/${match_id}_${replay_salt}.dem.bz2`,
        encoding: null
    };

    return request(options);
}

requestMatches(account_id, 30, 3, hero_id)
    .then(matches => {
        const matchIds = matches.map(({ match_id }) => match_id);
        const fetchReplays = matchIds.map(matchId => requestReplay(matchId));

        return Promise.all(fetchReplays)
    })
    .then(replayDetails => replayDetails
          .map((details) => downloadReplay(details[0])))

    .then(replays => replays
          .map((replay, i) => {
              var fn = replay.uri.pathname.slice(5);
              replay.pipe(fs.createWriteStream(fn));
          }))

    .then(() => console.log('complete'));
