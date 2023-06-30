function getFullAddress(text) {
    var response = $http.post($jsapi.context().injector.dadata.url,
        {
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Token " + $jsapi.context().injector.dadata.token,
                "X-Secret": "af02d6ef2befe14b84a74bf743f181a69ac534fa"
            },
            body: [text],
            dataType: "json"
        }
    );
    return response;
}
