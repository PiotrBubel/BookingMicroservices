'use strict';

myApp.factory("servicesFactory", function ($http, appConfig) {

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/services');
        },
        getDetails: function (name) {
            return $http.get(appConfig.apiAddress + '/services/' + name);
        },
        edit: function (name, serviceData) {
            return $http.put(appConfig.apiAddress + '/services/' + name, {service: serviceData});
        },
        create: function (serviceData) {
            return $http.post(appConfig.apiAddress + '/services', {service: serviceData});
        },
        remove: function (name) {
            return $http.delete(appConfig.apiAddress + '/services/' + name);
        }
    };
});