'use strict';

myApp.controller("bookingController", function ($scope, $timeout, $rootScope, servicesFactory, bookingFactory) {


    $scope.userLoggedIn = $rootScope.globalUser && $rootScope.globalUser.login;
    if ($scope.userLoggedIn) {
        $scope.userLogin = $rootScope.globalUser.login;
    }

    $scope.createNew = true;
    $scope.serviceData = {
        name: '',
        description: '',
        price: 1,
        timePeriod: 60,
        maxPeriods: 2
    };
    $scope.services = [];
    $scope.helper = {
        wholeDay: false
    };
    $scope.bookingDate = new Date();
    $scope.minDate = new Date();

    var clearData = angular.copy($scope.serviceData);
    var rawData = angular.copy($scope.serviceData);

    var messageHandler = {};
    messageHandler.showErrorMessage = function (message, description) {
        if (message) {
            $scope.errorMessage = message;
        } else {
            $scope.errorMessage = 'Błąd';
        }
        if (description) {
            $scope.errorDescription = description;
        } else {
            $scope.errorDescription = '';
        }
        $scope.showErrorMessage = true;
        $(".alert-danger").hide().show('medium');
        $timeout(function () {
            $scope.showErrorMessage = false;
        }, 3000);
    };

    messageHandler.showSuccessMessage = function (message, description) {
        if (message) {
            $scope.successMessage = message;
        } else {
            $scope.successMessage = 'Sukces';
        }
        if (description) {
            $scope.successDescription = description;
        } else {
            $scope.successDescription = '';
        }
        $scope.showSuccessMessage = true;
        $(".alert-success").hide().show('medium');
        $timeout(function () {
            $scope.showSuccessMessage = false;
        }, 3000);
    };

    var getServicesList = function () {
        servicesFactory.getList()
            .then(
                function (response) {
                    $scope.services = response.data.list;
                    $scope.changeSelected($scope.services[0]);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    getServicesList();

    $scope.changeSelected = function (name) {
        servicesFactory.getDetails(name)
            .then(
                function (response) {
                    $scope.serviceData = response.data;
                    rawData = response.data;
                    $scope.helper.wholeDay = ($scope.serviceData.maxPeriods * $scope.serviceData.timePeriod === 60 * 24);
                    $scope.createNew = false;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
});