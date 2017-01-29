'use strict';

myApp.controller("loginController", function ($scope, $timeout, usersFactory, loginFactory, $cookies, $location, $rootScope) {

    if ($rootScope.globalUser && $rootScope.globalUser.login) {
        $location.path('/account');
    }

    $scope.userData = {
        permissions: {
            canManageServices: false,
            canManageUsers: false,
            canManageBookings: false
        }
    };

    $scope.loginData = {};
    $scope.loginPattern = '[a-zA-Z]+';

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

    $scope.saveNewUser = function () {
        usersFactory.create($scope.userData)
            .then(
                function () {
                    $scope.login($scope.userData.login, $scope.userData.password);
                },
                function (error) {
                    if (error.data) {
                        if (error.data.message.includes('duplicate')) {
                            error.data.message = ' Użytkownik o podanym loginie już istnieje';
                        }
                        messageHandler.showErrorMessage('Błąd przy tworzeniu użytkownika ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.login = function (login, password) {
        loginFactory.login(login, password)
            .then(
                function (result) {
                    $cookies.putObject('token', result.data.token);
                    $rootScope.token = result.data.token;
                    usersFactory.getDetails(login)
                        .then(
                            function (result2) {
                                $cookies.putObject('user', result2.data);
                                $rootScope.globalUser = result2.data;
                                $location.path('/account');
                            },
                            function (error) {
                                if (error.data) {
                                    messageHandler.showErrorMessage('Błąd podczas pobierania szczegółów użytkownika', error.data.message);
                                } else {
                                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                                }
                            }
                        );
                },
                function (error) {
                    if (error.data) {
                        if (error.data.message.includes('No user with login') || error.data.message.includes('Wrong credentials.')) {
                            error.data.message = ' Podano zły login lub hasło';
                        }
                        messageHandler.showErrorMessage('Błąd podczas logowania', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
});