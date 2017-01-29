'use strict';

myApp.controller("bookingController", function ($scope, $timeout, $filter, $rootScope, servicesFactory, bookingFactory) {


    $scope.userLoggedIn = $rootScope.globalUser && $rootScope.globalUser.login;
    if ($scope.userLoggedIn) {
        $scope.userLogin = $rootScope.globalUser.login;
    }

    $scope.createNew = true;
    $scope.serviceData = {
        name: '',
        description: '',
        price: 1
    };
    $scope.services = [];
    $scope.bookingToSave = {
        bookingDate: new Date(),
        bookingDescription: ''
    };
    $scope.minDate = new Date();
    var notAvailableDates = [];

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
                    bookingFactory.getDates($scope.serviceData.name, $filter('date')(new Date(), 'yyyy')).then(
                        function (response) {
                            notAvailableDates = response.data.list;
                        },
                        function (error) {
                            if (error.data) {
                                messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                            } else {
                                messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                            }
                        });
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.dateFilter = function (date) {
        var stringDate = $filter('date')(date, 'yyyy-MM-dd');
        for (var i = 0; i < notAvailableDates.length; i++) {
            if (stringDate === notAvailableDates[i]) {
                return false;
            }
        }
        return true;
    };

    $scope.saveBooking = function () {
        var bookingToSave = {
            userLogin: $scope.userLogin,
            serviceName: $scope.serviceData.name,
            date: $filter('date')($scope.bookingToSave.bookingDate, 'yyyy-MM-dd'),
            description: $scope.bookingToSave.bookingDescription
        };
        bookingFactory.create(bookingToSave)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Dodano pomyślnie');
                    $scope.bookingToSave.bookingDescription = "";
                    $scope.bookingToSave.bookingDate = new Date();
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    }
});