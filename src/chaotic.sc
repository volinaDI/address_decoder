theme: /Yandex
    
    state: AskAddress
        q: * $yes * || fromState = "/Yandex/AskAddress/GetAddress"
        a: Назовите адрес
        
        state: GetAddress
            q: *
            script:
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $request.query = numeralsToNumbers($request.query);
                $temp.apiResponse = getResponseYandex($request.query);
            if: !$temp.apiResponse
                a: Не удалось получить ответ сервиса.
            else:
                script:
                    $temp.res = parseYandexRes($temp.apiResponse);
                    $session.query = $request.query;
                    $analytics.setComment(toPrettyString($temp.apiResponse));
                if: !$temp.res
                    a: Не нашлось такого адреса.
                    script: addLineTable($request.text, "-");
                    if: replaceFromDict($request.query, replacesYandex) != $request.query
                        go!: AddressWithReplace
                else:
                    script: addLineTable($request.text, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                    a: {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                    a: Это правильный ответ?

            state: AddressWithReplace
                q: * $no *
                script:
                    $temp.newQuery = replaceFromDict($session.query, replacesYandex);
                    $temp.apiResponse = getResponseYandex($temp.newQuery);
                if: !$temp.apiResponse
                    a: Не удалось получить ответ сервиса.
                else:
                    script:
                        $temp.res = parseYandexRes($temp.apiResponse);
                        $analytics.setComment(toPrettyString($temp.apiResponse));
                    if: !$temp.res
                        a: Не нашлось такого адреса.
                        script: addLineTable($request.text, "-");
                        go!: /Yandex/AskAddress
                    else:
                        script: addLineTable($temp.newQuery, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                        a: После замены получилось - {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                go!: /Yandex/AskAddress

theme: /Dadata
    
    state: AskAddress
        a: Назовите адрес
        
        state: GetAddress
            q: *
            script:
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $request.query = numeralsToNumbers($request.query);
                $temp.apiResponse = parseAddressDadata($request.query);
            a: {{$temp.apiResponse.result ? $temp.apiResponse.result : "Не нашлось такого адреса."}}
            script: addLineTable($request.text, $temp.apiResponse.result ? $temp.apiResponse.result : "-");
            go!: /Dadata/AskAddress