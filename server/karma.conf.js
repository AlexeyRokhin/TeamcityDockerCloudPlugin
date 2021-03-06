module.exports = function(config) {
    config.set({
        browsers: ['PhantomJS'],
        frameworks: ['jasmine-jquery', 'jasmine'],
        reporters: ['progress', 'junit', 'teamcity'],
        files: [
            'src/main/resources/buildServerResources/*.js',
            'src/test/js/*.js'
        ],

        junitReporter: {
            outputDir: 'build/test-results/karma'
        }

    });
};
