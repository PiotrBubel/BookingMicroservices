'use strict';

myApp.factory("usersFactory", function ($http, appConfig) {

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/users');
        },
        getDetails: function (name) {
            return $http.get(appConfig.apiAddress + '/users/' + name);
        },
        edit: function (name, userData) {
            return $http.put(appConfig.apiAddress + '/users/' + name, {user: userData});
        },
        create: function (userData) {
            return $http.post(appConfig.apiAddress + '/users', {user: userData});
        },
        remove: function (name) {
            return $http.delete(appConfig.apiAddress + '/users/' + name);
        }
    };
});