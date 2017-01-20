'use strict';

var myApp = angular.module('myApp', ['ngRoute', 'ngMessages', 'ngCookies', 'ngMaterial'])

    .config(['$routeProvider', '$locationProvider',
        function ($routeProvider, $locationProvider) {
            $routeProvider.when('/login', {
                controller: 'loginController',
                templateUrl: '/app/components/login/loginView.html'
            }).when('/services', {
                controller: 'servicesController',
                templateUrl: '/app/components/services/servicesView.html'
            }).when('/bookings', {
                controller: 'bookingController',
                templateUrl: '/app/components/booking/bookingView.html'
            }).when('/users', {
                controller: 'usersController',
                templateUrl: '/app/components/users/usersView.html'
            }).when('/account', {
                controller: 'accountController',
                templateUrl: '/app/components/account/accountView.html'
            }).when('/main', {
                controller: 'navbarController',
                templateUrl: '/app/components/main/mainView.html'
            }).otherwise({
                redirectTo: '/login'
            });
            $locationProvider.html5Mode(false).hashPrefix('!');
        }]);

myApp.controller("navbarController", function ($http, $scope, $cookies, $location, $rootScope) {
    $rootScope.globalUser = $cookies.getObject('user');
    $rootScope.token = $cookies.getObject('token');

    $scope.logout = function () {
        $cookies.remove('user');
        $cookies.remove('token');
        delete($rootScope.globalUser);
        delete($rootScope.token);
    };
});