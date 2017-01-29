'use strict';

myApp.controller("bookingManagementController", function ($scope, $timeout, $rootScope, servicesFactory, bookingFactory) {


    $scope.userLoggedIn = $rootScope.globalUser && $rootScope.globalUser.login;
    if ($scope.userLoggedIn) {
        $scope.userLogin = $rootScope.globalUser.login;
        $scope.canManageBookings = $rootScope.globalUser.permissions.canManageBookings;
    }

    var getBookingsList = function () {
        bookingFactory.getList()
            .then(
                function (response) {
                    $scope.bookings = response.data.list;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania listy usług ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    getBookingsList();

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

    $scope.removeBooking = function (id) {
        bookingFactory.remove(id)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Usunięto pomyślnie');
                    getBookingsList();
                    if($scope.selected && $scope.selected.id && id === $scope.selected.id)
                    delete($scope.selected);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd podczas usuwania rezerwacji ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

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
});