'use strict';

myApp.factory("bookingFactory", function ($http, $rootScope, appConfig) {

    var headers = function () {
        return {headers: {'Auth-Token': $rootScope.token}};
    };

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/bookings', headers());
        },
        getDetails: function (id) {
            return $http.get(appConfig.apiAddress + '/bookings/' + id, headers());
        },
        getDates: function (serviceName, datePrefix) {
            return $http.get(appConfig.apiAddress + '/bookings/dates/' + serviceName + '/' + datePrefix, headers());
        },
        edit: function (id, bookingData) {
            return $http.put(appConfig.apiAddress + '/bookings/' + id, {booking: bookingData}, headers());
        },
        create: function (bookingData) {
            return $http.post(appConfig.apiAddress + '/bookings', {booking: bookingData}, headers());
        },
        remove: function (id) {
            return $http.delete(appConfig.apiAddress + '/bookings/' + id, headers());
        }
    };
});