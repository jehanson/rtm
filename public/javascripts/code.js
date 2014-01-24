google.load('visualization', '1', {'packages': ['annotatedtimeline']});

$(document).ready(function () {

    var defaultBase = "VFINX";
    var defaultVariable = "BOGIX";

    var base = "";
    var variable = "";


    function setHash() {
        window.location.hash = variable + "/" + base;

    }


    $("#loadStuff").click(function (e) {
        e.preventDefault();

        base = $("#base").val();
        variable = $("#compare").val();

        console.log(base + " vs " + variable);

        var result = jQuery.ajax("/compare/" + base + "/" + variable);


        result.then(function (x) {

            var data = new google.visualization.DataTable();
            data.addColumn('date', 'Date');
            data.addColumn('number', variable + " vs " + base);

            var inputRows = x.split('\n');
            var outputRows = [];

            var min = 1;
            var max = 1;

            for (var i = 0; i < inputRows.length; i++) {
                var fields = inputRows[i].split(',');
                if (fields.length == 2) {
                    var date = new Date(fields[0]);
                    var value = Number(fields[1]);

                    if (value > max) max = value;
                    if (value < min) min = value;

                    outputRows.push([date, value]);
                }
            }

            data.addRows(outputRows);


            var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
            chart.draw(data, {displayAnnotations: true, min: min, max: max});

            setHash();
        });

        return false;
    });

    //read tickers from HASH
    function loadFromHash() {
        var tickers = window.location.hash.substr(1).split('/');
        if (tickers.length == 2) {
            base = tickers[1];
            variable = tickers[0];
        } else {
            base = "VFINX";
            variable = "BOGIX";
        }

        $("#base").val(base);
        $("#compare").val(variable);
        $("#loadStuff").click();
    }

    loadFromHash();

    window.onpopstate = loadFromHash
    window.onpushstate = loadFromHash





});