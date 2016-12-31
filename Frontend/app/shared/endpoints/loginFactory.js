'use strict';

myApp.factory("loginFactory", function ($http, appConfig) {

    return {
        login: function (login, password) {
            return $http.post(appConfig.apiAddress + '/authenticate', {login: login, password: password});
        }
    };
});