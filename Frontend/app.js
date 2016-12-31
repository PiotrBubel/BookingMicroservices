'use strict';

var myApp = angular.module('myApp', ['ngRoute', 'ngMessages', 'ngCookies'])

    .config(['$routeProvider', '$locationProvider',
        function ($routeProvider, $locationProvider) {
            $routeProvider.when('/login', {
                controller: 'loginController',
                templateUrl: '/app/components/login/loginView.html'
            }).when('/services', {
                controller: 'servicesController',
                templateUrl: '/app/components/services/servicesView.html'
            }).when('/bookings', {
                controller: 'navbarController',
                templateUrl: '/app/components/bookings/bookingsView.html'
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
    console.log('main controller');
    console.log('check cookies');
    var user = $cookies.getObject('user');
    console.log('cookie', user);
    $rootScope.globalUser = user;
    console.log('$rootScope.user', $rootScope.globalUser);

    $scope.logout = function () {
        //remove cookies
        $cookies.remove('user');
        delete($rootScope.globalUser);
        console.log('remove cookies');
    };
});