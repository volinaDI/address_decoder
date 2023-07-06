theme: /Yandex
    
    state: AskAddress
        q: * $yes * || fromState = "/Yandex/AskAddress/GetAddress"
        a: Назовите пожалуйста - реально существующий адрес - без указания почтового индекса 
        
        # state: Get
        #     q: * {[$addressCity] * ($addressStreet * $addressHome)} *
        #     q: * {$addressCity * ($addressStreet * [$addressHome])} *
        #     script:
        #         $session.query = $request.query;
        #         // если Тинькофф, надо пошаманить с числами
        #         if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($request.query);
        #         var dadataOk = true;
        #         // dadata
        #         // $temp.dadataResponse = dadataParseResponse(parseAddressDadata($session.query));
        #         // проверка страны на вменяемость
        #         // if (["Казахстан", "Россия"].indexOf($temp.dadataResponse.country) === -1) dadataOk = false;
        #     # a: {{toPrettyString($temp.dadataResponse)}}
        
        
        state: GetAddress
            q: * {[$addressCity] * ($addressStreet * $addressHome)} *
            q: * {$addressCity * ($addressStreet * [$addressHome])} *
            script:
                $session.query = $request.query;
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($request.query);
                $temp.apiResponse = getResponseYandex($session.query);
            if: !$temp.apiResponse
                a: Не удалось получить ответ сервиса.
            else:
                script:
                    $temp.res = parseYandexRes($temp.apiResponse);
                    $analytics.setComment(toPrettyString($temp.apiResponse));
                if: !$temp.res
                    a: Не нашлось такого адреса.
                    script: addLineTable($request.query, "-");
                    if: replaceFromDict($session.query, replacesYandex) != $session.query
                        go!: AddressWithReplace
                elseif: $temp.res[0]
                    script: addLineTable($request.query, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                    a: {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                    a: Это правильный ответ?
                elseif: $temp.res.GeoObject
                    script: addLineTable($request.query, $temp.res.GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                    a: {{$temp.res.GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                    a: Это правильный ответ?

            state: AddressWithReplace
                q: * $no *
                script:
                    $temp.newQuery = replaceFromDict($session.query.toLowerCase(), replacesYandex);
                    $temp.apiResponse = getResponseYandex($temp.newQuery);
                if: !$temp.apiResponse
                    a: Не удалось получить ответ сервиса.
                else:
                    script:
                        $temp.res = parseYandexRes($temp.apiResponse);
                        $analytics.setComment(toPrettyString($temp.apiResponse));
                    if: !$temp.res
                        a: Не нашлось такого адреса.
                        script: addLineTable($session.query, "-");
                        go!: /Yandex/AskAddress
                    else:
                        script: addLineTable($temp.newQuery, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                        a: После замены получилось - {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                script: delete $session.query;
                go!: /Yandex/AskAddress

        state: NoMatch
            event: noMatch
            a: Это не похоже на адрес, попробуйте ещё раз


# theme: /Dadata
    
#     state: AskAddress
#         a: Назовите адрес
        
#         state: GetAddress
#             q: *
#             script:
#                 // если Тинькофф, надо пошаманить с числами
#                 if ($injector.ASRmodel[$request.botId] === "tinkoff") $request.query = numeralsToNumbers($request.query);
#                 $temp.apiResponse = parseAddressDadata($request.query);
#             a: {{$temp.apiResponse.result ? $temp.apiResponse.result : "Не нашлось такого адреса."}}
#             script: addLineTable($request.query, $temp.apiResponse.result ? $temp.apiResponse.result : "-");
#             go!: /Dadata/AskAddress