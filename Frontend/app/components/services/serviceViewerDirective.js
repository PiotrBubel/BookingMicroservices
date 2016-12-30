/**
 * Created by pbubel on 30.12.16.
 */
myApp.directive('serviceViewer', function () {
    return {
        restrict: 'E',
        scope: {
            name: '@'
        },
        template:
        '<div id="serviceViewerTemplate">' +
            '<div ng-if="exists(serviceData.name)" class="row">' +
                '<div class="col-md-4">' +
                'Nazwa usługi:' +
                '</div>' +
                '<div class="col-md-8">' +
                '{{ serviceData.name }}' +
                '</div>' +
            '</div>' +

            '<div ng-if="exists(serviceData.description)" class="row">' +
                '<div class="col-md-4">' +
                'Opis usługi:' +
                '</div>' +
                '<div class="col-md-8">' +
                '{{ serviceData.description }}' +
                '</div>' +
            '</div>' +
            '<div ng-if="exists(serviceData.price)" class="row">' +
                '<div class="col-md-4">' +
                'Cena usługi (PLN/h):' +
                '</div>' +
                '<div class="col-md-8">' +
                '{{ serviceData.price }}' +
                '</div>' +
            '</div>' +
            '<div ng-if="exists(serviceData.time)" class="row">' +
                '<div ng-if="serviceData.time !== 60 * 24">' +
                    '<div class="col-md-4">' +
                    'Czas usługi:' +
                    '</div>' +
                    '<div class="col-md-8">' +
                    '{{ serviceData.suggestedTime }}' +
                    '</div>' +
                '</div>' +
                '<div ng-if="serviceData.time === 60 * 24">' +
                    '<div class="col-md-12 text-left">' +
                    'Usługa całodniowa' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>',
        controller: function ($scope, servicesFactory) {
            servicesFactory.getDetails($scope.name)
                .success(function (response) {
                    $scope.serviceData = response;
                    $scope.wholeDay = ($scope.serviceData.maxTime === 24 * 60) && ($scope.serviceData.minTime === $scope.serviceData.maxTime);
                });
        }
    }
});