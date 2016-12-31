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
        console.log('$scope.userData', $scope.userData);
        usersFactory.create($scope.userData)
            .success(function () {
                console.log('utworzono pomyślnie');
                $scope.login($scope.userData.login, $scope.userData.password);
            })
            .error(function (error) {
                if (error.message) {
                    messageHandler.showErrorMessage('Błąd ', error.message);
                } else {
                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                }
            });
    };

    $scope.login = function (login, password) {
        loginFactory.login(login, password)
            .success(function (result) {
                $cookies.putObject('user', result);
                $rootScope.globalUser = result;
                $location.path('/account');
            })
            .error(function (error) {
                if (error.message) {
                    messageHandler.showErrorMessage('Błąd ', error.message);
                } else {
                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                }
            });
    };
});