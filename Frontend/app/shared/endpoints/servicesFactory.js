'use strict';

myApp.factory("servicesFactory", function ($http, $rootScope, appConfig) {

    var headers = function () {
        return {headers: {'Auth-Token': $rootScope.token}};
    };

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/services', headers());
        },
        getDetails: function (name) {
            return $http.get(appConfig.apiAddress + '/services/' + name, headers());
        },
        edit: function (name, serviceData) {
            return $http.put(appConfig.apiAddress + '/services/' + name, {service: serviceData}, headers());
        },
        create: function (serviceData) {
            return $http.post(appConfig.apiAddress + '/services', {service: serviceData}, headers());
        },
        remove: function (name) {
            return $http.delete(appConfig.apiAddress + '/services/' + name, headers());
        }
    };
});