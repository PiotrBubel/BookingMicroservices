'use strict';

myApp.factory("usersFactory", function ($http, $rootScope, appConfig) {

    var headers = function () {
        return {headers: {'Auth-Token': $rootScope.token}};
    };

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/users', headers());
        },
        getDetails: function (name) {
            return $http.get(appConfig.apiAddress + '/users/' + name, headers());
        },
        edit: function (name, userData) {
            return $http.put(appConfig.apiAddress + '/users/' + name, {user: userData}, headers());
        },
        create: function (userData) {
            return $http.post(appConfig.apiAddress + '/users', {user: userData});
        },
        remove: function (name) {
            return $http.delete(appConfig.apiAddress + '/users/' + name, headers());
        }
    };
});