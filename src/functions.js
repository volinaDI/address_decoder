function parseAddressDadata(text) {
    var params = $jsapi.context().injector.dadata;
    var response = $http.post(params.url,
        {
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Token " + params.token,
                "X-Secret": params.secret
            },
            body: [text],
            dataType: "json"
        }
    );
    $analytics.setComment(toPrettyString(response));
    return response;
}

// yandex
function getResponseYandex(text) {
    var params = $jsapi.context().injector.yandex;
    var response = $http.get(params.url
        + "?apikey=" + params.token
        + "&geocode=" + formatQueryYandex(text) +
        + "&&format=json");
    if (response && response.data && response.data.ymaps) return response.data.ymaps;
}


function parseYandexRes(obj) {
    if (obj && obj.GeoObjectCollection 
      && obj.GeoObjectCollection.metaDataProperty.GeocoderResponseMetaData
      && obj.GeoObjectCollection.metaDataProperty.GeocoderResponseMetaData.found === 0) {
        return false;
    }
    if (obj && obj.GeoObjectCollection 
      && obj.GeoObjectCollection.featureMember) {
        return obj.GeoObjectCollection.featureMember;
    }
}

function formatQueryYandex(text) {
    return text.replace(/\s/g, "+");
}

function replaceFromDict(text, dict) {
    var res = text;
    var wordsToReplace = _.keys(dict);
    wordsToReplace.forEach(function(wordToReplace) {
        res = res.replace(wordToReplace, dict[wordToReplace]);
    })
    return res;
}
