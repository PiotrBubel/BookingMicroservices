'use strict';

myApp.factory("bookingFactory", function ($http, $rootScope, appConfig) {

    var headers = function () {
        return {headers: {'Auth-Token': $rootScope.token}};
    };

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/booking', headers());
        },
        getDetails: function (id) {
            return $http.get(appConfig.apiAddress + '/booking/' + id, headers());
        },
        edit: function (id, bookingData) {
            return $http.put(appConfig.apiAddress + '/booking/' + id, {booking: bookingData}, headers());
        },
        create: function (bookingData) {
            return $http.post(appConfig.apiAddress + '/services', {booking: bookingData}, headers());
        },
        remove: function (id) {
            return $http.delete(appConfig.apiAddress + '/services/' + id, headers());
        }
    };
});