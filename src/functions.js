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
        if (city === "") {
            
        }
    } else if (obj.city_type_full) {
        cityType = obj.city_type_full;
        city = obj.city;
    } else if (obj.settlement_type_full) {
        cityType = obj.settlement_type_full;
        city = obj.settlement;
    }
    var res = {
        "country": obj.country,
        // "region": obj.region,
        // "regionType": obj.region_type_full,
        "city": city,
        "cityType": cityType,
        "street": city === "Кумертау" && obj.settlement_type_full && obj.settlement_type_full == "сквер" ? obj.settlement : obj.street,
        "streetType": city === "Кумертау" && obj.settlement_type_full && obj.settlement_type_full == "сквер" ? "сквер" : obj.street_type_full,
        "house": obj.house,
        "houseType": obj.house_type_full,
        "houseAdd": obj.block_type_full ? obj.block_type_full + " " + obj.block : undefined,
        "postalIndex": obj.postal_code
        
    }
    return res;
}

function formAddreessToSay(addressObj) {
    var res = addressObj.country;
    if (addressObj.city && addressObj.cityType) res += ", " + addressObj.cityType + " " + addressObj.city;
    else return res;
    
    if (addressObj.street && addressObj.streetType) res += ", " + addressObj.streetType + " " + addressObj.street;
    else return res;
    
    if (addressObj.house) res += ", " + addressObj.house;
    else return res;
    
    if (addressObj.houseAdd) res += " " + addressObj.houseAdd;
    
    // if (addressObj.postalIndex) res += " почтовый индекс " + addressObj.postalIndex;
    
    return res;
    
}

// yandex
function getResponseYandex(text) {
    var params = $jsapi.context().injector.yandex;
    var response = $http.get(params.url
        + "?apikey=" + params.token
        + "&geocode=" + formatQueryYandex(text) +
        + "&result=1" +
        + "&&format=json");
    if (response && response.data && response.data.ymaps) return response.data.ymaps;
}


function parseYandexGeoObject(obj) {
    if (obj && obj.GeoObjectCollection 
      && obj.GeoObjectCollection.metaDataProperty.GeocoderResponseMetaData
      && obj.GeoObjectCollection.metaDataProperty.GeocoderResponseMetaData.found === 0) {
        return false;
    }
    if (obj && obj.GeoObjectCollection 
      && obj.GeoObjectCollection.featureMember
      && obj.GeoObjectCollection.featureMember[0]
      && obj.GeoObjectCollection.featureMember[0].GeoObject) {
        return obj.GeoObjectCollection.featureMember[0].GeoObject;
    }
}

function yandexComponents(geoObject) {
    if (geoObject && geoObject.metaDataProperty
      && geoObject.metaDataProperty.GeocoderMetaData
      && geoObject.metaDataProperty.GeocoderMetaData.Address
      && geoObject.metaDataProperty.GeocoderMetaData.Address.Component) {
        var addressComponents = geoObject.metaDataProperty.GeocoderMetaData.Address.Component;
    }
    if (addressComponents[0].kind === "country" && addressComponents[0].name !== "Казахстан") return false;
    var res = {country: "Казахстан"};
    addressComponents.forEach(function(component) {
        if (component.kind && component.kind === "locality") {
            res.cityType = "город",
            res.city = component.name
        } else if (component.kind && component.kind === "street") {
            var streetPatterns = $nlp.match(component.name, "/StepByStep/AskStreet/Get");
            // $reactions.answer(streetPatterns._streetType && true == true);
            // $reactions.answer(toPrettyString(streetPatterns));
            if (streetPatterns && streetPatterns.parseTree._streetType) {
                res.street = streetPatterns.parseTree._streetName.replace(streetPatterns.parseTree._streetType, "");
                res.streetType = streetPatterns.parseTree._streetType;
            } else { 
                res.street = component.name;
                res.streetType = "улица";
            }
        }
    });
    return res;
}

function yandexFormattedAddress(geoObject) {
    if (geoObject && geoObject.metaDataProperty
      && geoObject.metaDataProperty.GeocoderMetaData
      && geoObject.metaDataProperty.GeocoderMetaData.Address
      && geoObject.metaDataProperty.GeocoderMetaData.Address.formatted) {
        return geoObject.metaDataProperty.GeocoderMetaData.Address.formatted;
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
            "range": "'Вольный формат'!A",
            "index": rowNum,
            "values": [
                // asr ? asr : "текстовый ввод", // не показываем клиенту
                // $jsapi.context().client.api, // не показываем клиенту
                request,
                result // результат парсится на кусочки обоими API, индекс тоже выуживается, просто пока не дошли руки
            ],
            "sheet": sheet2bot.sheetId
        },
        headers: {"Content-Type": "application/json"}
        });
}

function addFullLineTable(request, result, country, city, street, house) {
    // какая у нас модель asr
    var sheet2bot = $jsapi.context().injector.sheet2bot;
    var asr = $jsapi.context().injector.ASRmodel[$jsapi.context().request.botId];
    // первый пустой ряд в таблице
    var urlRowNum = sheet2bot.http + "rows?sheet=" + sheet2bot.sheetId + "&range=" + sheet2bot.cellNextRow;
    var rowNum = Number($http.get(urlRowNum, {headers: {"Content-Type": "application/json"}}).data[0]);
    // заполняем таблицу
    var response = $http.post(sheet2bot.http + "update/", {
        body: {
            "range": "'Вольный формат'!A",
            "index": rowNum,
            "values": [
                // asr ? asr : "текстовый ввод", // не показываем клиенту
                // $jsapi.context().client.api, // не показываем клиенту
                request,
                result,
                country, city, street, house
            ],
            "sheet": sheet2bot.sheetId
        },
        headers: {"Content-Type": "application/json"}
        });
}

function isBauBaksha(text) {
    var entities = $caila.entitiesLookup(text, true).entities
    var res = false;
    entities.forEach(function(entityElem) {
        if (entityElem.entity === "bauBaksha") res = true;
    });
    return res;
} 
