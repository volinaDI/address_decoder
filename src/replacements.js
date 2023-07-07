function chaoticAddressReplace(text){
    var dict = {
        "цниимод": /(((([сц] ?)?[мн][ие]?|сним|см[ие]н)?и|синяя|миит|[сц] н?ей) ?мо[тдц]|кинемат( кинемат)?)/,
        "нерис саломеи": /(мерил?|нереста? а?л[ао]меи|нерисоми|не ?рис с[ао]лом[ие][ияй]?( я)?|нерис[ао]вани|меритоломеи|(нери|мере) соловей|н[ае]рис[ао]?л[ао]м[еи][ия])/,
        "джавахарлала неру": /[^\s]{0,5}((з|дж)а(в[оа])?р?ха(рл?|р?л)а[вл]а|харлала|д?ж[ие]вахар лала) ([мн][еи]р[оу]|мир)/,
        "астана": /[н][уо]р[\- ]?султан[^\s]?/,
        "новобиккино": /(на ?баб[иe] ?кино|новолюбить ?кино|новоб[еи]т?кино|новоб[еи]тк?ино)/,
        "яшьлек": /(яр?[щш]и?л[еы]к|я[щш]нек)/,
        "торайгырова": /(султан ?махму[дт]а? )?(т[аоу]райг[иы]р(гы)?(о|з[еио])ва|2 г[ыи]рова|т[ао]радилова|т[ао]йбурова|т[ао]йрадмирова|т[аоу]йраг[иы]рова)( султан ?(махму|хуму)[дт]а?)?/,
        "верхненовокутлумбетьево": /(верхнее? ?(нов(о|ый)|молоко)|верхнего) ?(к[оу]н?[зтл]л?|ту[рл])ум?бл?[еи](е|[кт][еь]?е?)в[ао]/,
        "спасоглинищевский": /спасо[бг]лиз?ни?[чщ]евский/,
        "космодамианская набережная": /(набережная )?космодемьянская( набережная)?/,
        "ыбырая алтынсарина": /((а сырая|[ауыэи]б[иы]ра?й?я?|выборгская|[ауыэи]б[иы]?рай?я|[иыэ]рымбая) алты[рн][ст]а(ры)?р?ина|[аыэ]?бара(я|ева) кенсарина)/,
        "шугыла бау-бакша cериктестиги": /([сш][иыу]р?г?р?[аиыо] ?с?л[ао]? ?|ш[иые]гу)?бао?[оу]м?[- ]?ба([кб]ш|шк)а(си)? ?(с )?(с[еи]р[еи]?к?т?.?[аеиы](т[ие])?[сш]т[еиы]ги?|с[еи]р[еи]?кк?( ?та)? ([иэ]?ст[еи]ги)?|с[еи]р[еи]?кк?|с[еи]р[еи]?кк?|сергей( сергей?)|с[еи](кр|р[еи]?)инский?|с[еи]р[еи]к.?( ?и? ?т[ие]с[иея]ги)?)?/,
        "шарикоподшипниковская": /шарикоподшипников(ое|ск.?.?)/,
        "шораяк омара": /(ш[ао]?р[аое] ?й?[аея]к([тд]ы[нм]?)?|ш[ао]й?[иы]тын|ш[иеэао]р[оа]й?[иыя]к?ц[иы]н) ?([ао]|на)? ([ао]?мар[аыэ]?н?|марве)/,
        "кызылорда": /кызылорда[нл]/,
        "рахимжана кошкарбаева": /(карам? ?)?((ка)?ра[гк]а|р?а?[кх][иы])[мн]ы?д??[жш]ана? к[ао][жш][аие]? ?к[ао]р?ба[еи]ва/,
        "кокшетау": /к[ао]к?ш[еи]к?та[ул]/,
        "кумертау сквер": /кумертауск ?[иэ]р/,
        "сквер шагита ахметовича худайбердина": /((сквер|тверь)( имени)? (([сш]агита?|хадита|ша[гк]и[дт]а)( ахм[ае][тд]овича)?|шаги тахм[ае][тд]овича) [кх][оу]д[ао]й?бердина|( имени)? (([сш]агита?|хадита|ша[гк]и[дт]а)( ахм[ае][тд]овича)?|шаги тахм[ае][тд]овича) [кх][оу]д[ао]й?бердина (сквер|тверь))/,
        "ризаэтдина фахретдина": /(р?из[ае] ?|риз[ае]й? ?|изо|из-за)((эт[ио]( ха)? сергеев.?)|[еэ]?(ль|ти)?[тд]{1,2}ин(ов)?а|[тд]?зина)( фа[кх]рет?д?ина| по ?средин.)?/,
        "квесисская 2": /к?вест?ит?ская 2/,
        "советская": /сем[ьи]градская/,
        "гельсингфорсская": /гельфоровска./,
        "ясеневая": /[ия]с[еи]невая/
    }
    _.keys(dict).forEach(function(elem) {
        text = text.toLowerCase().replace(dict[elem], elem);
    });
    text = text.toLowerCase().replace(/(^(индекс )?((\d ?\d ?){3})|индекс ((\d ?\d ?){2}(\d ?\d)))/, "");
    return text;
}

function capitalize(string) {
    var res = "";
    var resArr = string.split(" ");
    resArr.forEach(function(word) {
        if (res) res = res + " ";
        res += word.slice(0,1).toUpperCase() + word.slice(1);
    });
    return res;
}
