// dadata
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
    if (response && response.data) return response.data[0];
}
// dadata parse response
function dadataParseResponse(obj) {
    var cityType;
    var city;
    if (obj.region_type_full == "город") {
        cityType = "город";
        city = obj.region;
    } else if (obj.city_type_full) {
        cityType = obj.city_type_full;
        city = obj.city;
    } else if (obj.settlement_type_full) {
        cityType = obj.settlement_type_full;
        city = obj.settlement;
    }
    var res = {
        "country": obj.country,
        "city": city,
        "cityType": cityType,
        "street": obj.street,
        "streetType": obj.street_type_full,
        "house": obj.house,
        "houseType": obj.house_type_full,
        "house add": obj.block_type_full ? obj.block_type_full + " " + obj.block : undefined,
        "index": obj.postal_code
        
    }
    return res;
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

// tinkoff asr
function numeralsToNumbers(text) {
    var res = $caila.entitiesLookup(text, true);
    res.entities.forEach(function(elem) {
        if (elem.entity === "zb.number") {
            text = text.replace(elem.text, parseInt(elem.value))
        }
    })
    return text.replace("дробь", "/");
}

// табличка
function addLineTable(request, result) {
    // какая у нас модель asr
    var sheet2bot = $jsapi.context().injector.sheet2bot;
    var asr = $jsapi.context().injector.ASRmodel[$jsapi.context().request.botId];
    // первый пустой ряд в таблице
    var urlRowNum = sheet2bot.http + "rows?sheet=" + sheet2bot.sheetId + "&range=" + sheet2bot.cellNextRow;
    var rowNum = Number($http.get(urlRowNum, {headers: {"Content-Type": "application/json"}}).data[0]);
    // заполняем таблицу
    var response = $http.post(sheet2bot.http + "update/", {
        body: {
            "range": "'dev'!A",
            "index": rowNum,
            "values": [
                asr ? asr : "текстовый ввод",
                $jsapi.context().client.api,
                request,
                result // результат парсится на кусочки обоими API, индекс тоже выуживается, просто пока не дошли руки
            ],
            "sheet": sheet2bot.sheetId
        },
        headers: {"Content-Type": "application/json"}
        });
}