theme: /Yandex
    
    state: AskAddress
        q: * $yes * ||fromState = "/Yandex/AskAddress/GetAddress"
        a: Назовите адрес
        
        state: GetAddress
            q: *
            script: $temp.apiResponse = getResponseYandex($request.query);
            if: !$temp.apiResponse
                a: Не удалось получить ответ сервиса.
            else:
                script:
                    $temp.res = parseYandexRes($temp.apiResponse);
                    $session.query = $request.query;
                    $analytics.setComment(toPrettyString($temp.apiResponse));
                if: !$temp.res
                    a: Не нашлось такого адреса.
                    if: replaceFromDict($request.query, replacesYandex) != $request.query
                        go!: AddressWithReplace
                else:
                    a: {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                    a: Это правильный ответ?

            state: AddressWithReplace
                q: * $no *
                a: После замены получилось - {{replaceFromDict($session.query, replacesYandex)}}
                go!: /Yandex/AskAddress

theme: /Dadata
    
    state: AskAddress
        a: Назовите адрес
        
        state: GetAddress
            q: *
            script: $temp.apiResponse = parseAddressDadata($request.query);
            a: {{$temp.apiResponse.result ? $temp.apiResponse.result : Не нашлось такого адреса.}}
            # if: !$temp.apiResponse
                # a: Не удалось получить ответ сервиса.
        