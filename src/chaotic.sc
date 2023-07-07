theme: /Address
    
    state: Ask
        a: Назовите адрес
        
        state: Get
            q: * {[$addressCity/$City] * ($addressStreet * $addressHome)} *
            q: * {($addressCity/$City) * ($addressStreet * [$addressHome])} *
            script:
                delete $session.dadataRes;
                delete $session.addressAnswer;
                $session.query = $request.query.replace(/[Лл]итера /, "").replace(/[Лл]итер.?.?/, "").replace(/номер /, "");
                $session.firstRequest = $request.query;
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($session.query);
                // dadata
                $session.dadataResponse = parseAddressDadata($session.query);
                $temp.dadataOk = true;
            # dadata не отвечает
            if: !$session.dadataResponse
                a: Произошла техническая ошибка. Нет доступа к базе данных
                a: Перезвоните пожалуйста.
                script: $response.replies.push({"type": "hangup"});
            else: 
                script:
                    $session.dadataRes = dadataParseResponse($session.dadataResponse);
                    // проверка страны на вменяемость
                    if (["Казахстан", "Россия", "Беларусь", "Армения", "Молдова, Республика"].indexOf($session.dadataRes.country) === -1) {
                        $temp.dadataOk = false;
                        $temp.incorrectCountry = true;
                    }
                    if (!$session.dadataRes.street || !$session.dadataRes.house) {
                        $temp.dadataOk = false;
                        // $reactions.answer("dss" + toPrettyString($session.dadataRes.street));
                    }
                    // формулируем ответ
                    $session.addressAnswer = formAddreessToSay($session.dadataRes);
                if: $temp.dadataOk
                    a: {{$session.addressAnswer}}. Это правильный адрес?
                else:
                    if: $temp.incorrectCountry
                        a: Давайте я попробую записать адрес по частям.
                        go!: /StepByStep/AskCountry
                    if: $session.dadataRes.city && !$session.dadataRes.street
                        go!: OnlyCity
            
            state: Yes
                q: * $yes * 
                script: 
                    // заполнение таблицы
                    addLineTable($session.firstRequest, $session.dadataResponse.result);
                    delete $session.firstRequest;
                go!: /Address/Ask
            
            state: No
                q: * $no *
                a: Очень жаль. Давайте я попробую записать адрес по частям.
                go!: /StepByStep/AskCountry
                
            state: NoMatch
                event: noMatch
                event: speechNotRecognized
                a: Простите - не расслышала. {{$session.addressAnswer}} - это правильный адрес?
                go: /Address/Ask/Get
                
            state: OnlyCity
                a: Я поняла только часть адреса. {{$session.addressAnswer}} - это правильно?
                
                state: Correct
                    q: * $yes *
                    script:
                        $session.country = $session.dadataRes.country;
                        $session.city = $session.dadataRes.city;
                        $session.cityType = $session.dadataRes.cityType;
                    a: Хорошо, помогите мне пожалуйста записать полный адрес
                    go!: /StepByStep/AskStreet
                    
                state: Incorrect
                    q: *
                    event: speechNotRecognized
                    a: Очень жаль. Давайте я попробую записать адрес по частям.
                    script: delete $session.dadataRes;
                    go!: /StepByStep/AskCountry
                
            state: OnlyStreet
                a: Я поняла только часть адреса. {{$session.addressAnswer}} - это правильно?
                
                state: Correct
                    q: * $yes *
                    script:
                        $session.country = $session.dadataRes.country;
                        $session.city = $session.dadataRes.city;
                        $session.cityType = $session.dadataRes.cityType;
                        $session.street = $session.dadataRes.street;
                        $session.streetType = $session.dadataRes.streetType;
                    a: Хорошо, помогите мне пожалуйста записать полный адрес
                    go!: /StepByStep/AskHouseNumber
                    
                state: Incorrect
                    q: *
                    event: speechNotRecognized
                    a: Очень жаль. Давайте я попробую записать адрес по частям.
                    script: delete $session.dadataRes;
                    go!: /StepByStep/AskCountry
        
        state: NoMatch
            event: noMatch
            event: speechNotRecognized
            # script: delete $session.dadataRes;
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