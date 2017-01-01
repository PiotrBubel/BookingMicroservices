'use strict';

myApp.factory("bookingFactory", function ($http, appConfig) {

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/booking');
        },
        getDetails: function (id) {
            return $http.get(appConfig.apiAddress + '/booking/' + id);
        },
        edit: function (id, bookingData) {
            return $http.put(appConfig.apiAddress + '/booking/' + id, {booking: bookingData});
        },
        create: function (bookingData) {
            return $http.post(appConfig.apiAddress + '/services', {booking: bookingData});
        },
        remove: function (id) {
            return $http.delete(appConfig.apiAddress + '/services/' + id);
        }
    };
});