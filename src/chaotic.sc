theme: /Address
    
    state: Ask
        q: * $yes * || fromState = "/Address/Ask/Get"
        # a: Назовите пожалуйста - реально существующий адрес - без указания почтового индекса 
        a: Назовите адрес
        
        state: Get
            q: * {[$addressCity/$City] * ($addressStreet * $addressHome)} *
            q: * {($addressCity/$City) * ($addressStreet * [$addressHome])} *
            script:
                $session.query = $request.query.replace(/[Лл]итера /, "").replace(/[Лл]итер.?.?/, "");
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($request.query);
                $temp.dadataOk = true;
                // dadata
                $temp.dadataResponse = parseAddressDadata($session.query);
            # dadata не отвечает
            if: !$temp.dadataResponse
                a: Произошла техническая ошибка. Нет доступа к базе данных
                a: Перезвоните пожалуйста.
                script: $response.replies.push({"type": "hangup"});
            else: 
                script:
                    $temp.dadataRes = dadataParseResponse($temp.dadataResponse);
                    // проверка страны на вменяемость
                    if (["Казахстан", "Россия"].indexOf($temp.dadataRes.country) === -1) $temp.dadataOk = false;
                    if (!$temp.dadataRes.street || !$temp.dadataRes.house) $temp.dadataOk = false;
                    addLineTable($request.query, $temp.dadataResponse.result);
                    # заполнение таблицы
                    
                    
                a: {{$temp.dadataOk ? $temp.dadataResponse.result + ".Это правильный ответ?" : "Извините, не могу найти адрес в базе данных. Вы сказали " + $session.query + ". Верно?"}}

            state: No
                q: * $no *
                a: Очень жаль. Попробуем ещё раз?
                go!: /Address/Ask
        
        state: NoMatch
            event: noMatch
            event: speechNotRecognized
            a: Это не похоже на адрес, попробуйте ещё раз. Назовите пожалуйста реально существующий адрес - без указания квартиры, этажа и почтового индекса.
            go!: /Address/Ask

        
        # state: Get
        #     q: * {[$addressCity] * ($addressStreet * $addressHome)} *
        #     q: * {$addressCity * ($addressStreet * [$addressHome])} *
        #     script:
        #         $session.query = $request.query;
        #         // если Тинькофф, надо пошаманить с числами
        #         if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($request.query);
        #         $temp.apiResponse = getResponseYandex($session.query);
        #     if: !$temp.apiResponse
        #         a: Не удалось получить ответ сервиса.
        #     else:
        #         script:
        #             $temp.res = parseYandexRes($temp.apiResponse);
        #             $analytics.setComment(toPrettyString($temp.apiResponse));
        #         if: !$temp.res
        #             a: Не нашлось такого адреса.
        #             script: addLineTable($request.query, "-");
        #             if: replaceFromDict($session.query, replacesYandex) != $session.query
        #                 go!: AddressWithReplace
        #         elseif: $temp.res[0]
        #             script: addLineTable($request.query, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
        #             a: {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
        #             a: Это правильный ответ?
        #         elseif: $temp.res.GeoObject
        #             script: addLineTable($request.query, $temp.res.GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
        #             a: {{$temp.res.GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
        #             a: Это правильный ответ?

            # state: AddressWithReplace
            #     q: * $no *
            #     script:
            #         $temp.newQuery = replaceFromDict($session.query.toLowerCase(), replacesYandex);
            #         $temp.apiResponse = getResponseYandex($temp.newQuery);
            #     if: !$temp.apiResponse
            #         a: Не удалось получить ответ сервиса.
            #     else:
            #         script:
            #             $temp.res = parseYandexRes($temp.apiResponse);
            #             $analytics.setComment(toPrettyString($temp.apiResponse));
            #         if: !$temp.res
            #             a: Не нашлось такого адреса.
            #             script: addLineTable($session.query, "-");
            #             go!: /Address/Ask
            #         else:
            #             script: addLineTable($temp.newQuery, $temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
            #             a: После замены получилось - {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
            #     script: delete $session.query;
            #     go!: /Address/Ask



# theme: /Dadata
    
#     state: Ask
#         a: Назовите адрес
        
#         state: Get
#             q: *
#             script:
#                 // если Тинькофф, надо пошаманить с числами
#                 if ($injector.ASRmodel[$request.botId] === "tinkoff") $request.query = numeralsToNumbers($request.query);
#                 $temp.apiResponse = parseAddressDadata($request.query);
#             a: {{$temp.apiResponse.result ? $temp.apiResponse.result : "Не нашлось такого адреса."}}
#             script: addLineTable($request.query, $temp.apiResponse.result ? $temp.apiResponse.result : "-");
#             go!: /Dadata/Ask