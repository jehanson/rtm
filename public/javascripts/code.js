google.load('visualization', '1', {'packages': ['annotatedtimeline']});

$(document).ready(function () {
    $("#loadStuff").click(function (e) {
        e.preventDefault();

        var baseTicker = $("#base").val();
        var compareTicker = $("#compare").val();

        console.log(baseTicker + " vs " + compareTicker);

        var result = jQuery.ajax("/compare/" + baseTicker + "/" + compareTicker);


        result.then(function (x) {

            var data = new google.visualization.DataTable();
            data.addColumn('date', 'Date');
            data.addColumn('number', baseTicker + " vs " + compareTicker);

            var inputRows = x.split('\n');
            var outputRows = [];

            var min = 0;
            var max = 0;

            for (var i = 0; i < inputRows.length; i++) {
                var fields = inputRows[i].split(',');
                if (fields.length == 2) {
                    var date = new Date(fields[0]);
                    var value = Number(fields[1]) -1;

                    if (value > max) max = value;
                    if (value < min) min = value;

                    outputRows.push([date, value]);
                }
            }

            data.addRows(outputRows);


            var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
            chart.draw(data, {displayAnnotations: true, min: min, max: max});

        });

        return false;
    });
});