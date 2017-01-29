'use strict';

myApp.controller("accountController", function ($scope, $timeout, bookingFactory, servicesFactory, usersFactory, $rootScope) {

    $scope.userLoggedIn = $rootScope.globalUser && $rootScope.globalUser.login;
    if ($scope.userLoggedIn) {
        $scope.userLogin = $rootScope.globalUser.login;

        usersFactory.getDetails($scope.userLogin).then(function (response) {
            $scope.userData = response.data;
        }, function (error) {
            if (error.data) {
                messageHandler.showErrorMessage('Błąd pobierania danych użytkownika', error.data.message);
            } else {
                messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
            }
        });

        $scope.getBooking = function (id) {
            bookingFactory.getDetails(id)
                .then(
                    function (response) {
                        $scope.selected = response.data;
                        servicesFactory.getDetails($scope.selected.serviceName)
                            .then(function (response) {
                                    $scope.selected.service = response.data;
                                },
                                function (error) {
                                    if (error.data) {
                                        messageHandler.showErrorMessage('Błąd pobierania szczegółów usługi ', error.data.message);
                                    } else {
                                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                                    }
                                })
                    },
                    function (error) {
                        if (error.data) {
                            messageHandler.showErrorMessage('Błąd pobierania szczegółów rezerwacji ', error.data.message);
                        } else {
                            messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                        }
                    });
        };

        var getBookingsList = function () {
            bookingFactory.getList($scope.userLogin)
                .then(
                    function (response) {
                        $scope.bookings = response.data.list;
                        if ($scope.bookings && $scope.bookings[0] && $scope.bookings[0].id) {
                            $scope.getBooking($scope.bookings[0].id);
                        }
                    },
                    function (error) {
                        if (error.data) {
                            messageHandler.showErrorMessage('Błąd pobierania listy rezerwacji ', error.data.message);
                        } else {
                            messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                        }
                    });
        };
        getBookingsList();

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
    }
});