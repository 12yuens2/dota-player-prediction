const Bottleneck = require('bottleneck');
const rp = require("request-promise-native");
const request = require("request");
const fs = require('fs');
const util = require('util');

const hero_id = process.argv[2];
const num_players = process.argv[3];
const num_games = process.argv[4];
const SCRATCH = process.argv[5];


const requestPlayersUnlimited = (hero_id) => {
    const options = {
        uri: `https://api.opendota.com/api/heroes/${hero_id}/players`,
        json: true
    };

    return rp(options);
}

const requestPlayerUnlimited = (account_id) => {
    const options = {
        uri: `https://api.opendota.com/api/players/${account_id}`,
        json: true
    };

    return rp(options);
}

const requestMatchesUnlimited = (account_id, limit=50, game_mode=3, hero_id) => {
    const options = {
        uri: `https://api.opendota.com/api/players/${account_id}/matches`,
        qs: {
            "limit": limit,
            "hero_id": hero_id,
            "game_mode": game_mode
        },
        json: true
    };

    if (!hero_id) {
        options.qs.hero_id = hero_id;
    }

    return rp(options);
}

const requestReplayUnlimited = (match_id) => {
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


//const SCRATCH = `/cs/scratch/sy35/dota-data/${hero_id}/replays/`;


const limiter = new Bottleneck({
    maxConcurrent: 2,
    minTime: 2500
});

const requestPlayers = limiter.wrap(requestPlayersUnlimited);
const requestPlayer = limiter.wrap(requestPlayerUnlimited);
const requestMatches = limiter.wrap(requestMatchesUnlimited);
const requestReplay = limiter.wrap(requestReplayUnlimited);

requestPlayers(hero_id)
    .then(playerInfos => {
        const players = playerInfos.slice(0, num_players);
        const accountIds = players.map(({ account_id }) => account_id);
        const fetchPlayers = accountIds.map(accountId => requestPlayer(accountId));

        return Promise.all(fetchPlayers)
    })
    .then(playerDetails => {
        const ids = playerDetails.map((playerDetail) => [playerDetail["profile"]["account_id"], playerDetail["profile"]["steamid"]])

        ids.map(([account_id, steam_id]) => requestMatches(account_id, num_games, 3, hero_id)
                .then(matches => {
                    const filtered_matches = matches.filter(match => match["start_time"] < 1542499200)

                    const matchIds = filtered_matches.map(({ match_id }) => match_id);
                    const fetchReplays = matchIds.map(matchId => requestReplay(matchId));

                    return Promise.all(fetchReplays)
                 })
                 .then(replayDetails => replayDetails.map((details) => downloadReplay(details[0])))
                 .then(replays => replays
                       .map((replay) => {
                           var filename = SCRATCH + steam_id + "-" + replay.uri.pathname.slice(5);
                           replay.pipe(fs.createWriteStream(filename));

                           return filename;
                       }))
                 .then((filenames) => console.log(filenames.length)))
    });

