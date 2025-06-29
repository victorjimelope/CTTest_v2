/**
 * 
 * 
 * @author victorjl 20250605
 */
function drawCircularProgressBar(containerId, progress) {
	
	var data = google.visualization.arrayToDataTable([
	    ["", ""],
	    ["", progress],
	    ["", 100 - progress],
	]);

	var options = {
	    pieHole: 0.6,
	    legend: "none",
	    pieSliceText: "none",
	    tooltip: { trigger: "none" },
	    enableInteractivity: false,
	    colors: ["#183153", "Gainsboro"],
	};

	const chart = new google.visualization.PieChart(document.getElementById(containerId));
	chart.draw(data, options);
	
}

function drawColumnChart(containerId, notas) {
	
    const bins = Array(11).fill(0);
    notas.forEach((nota) => {
        if (nota === 10) {
            bins[10]++;
        } else {
            bins[Math.floor(nota)]++;
        }
    });
	
    const dataArray = [["Nota", "Alumnos"]];
    for (let i = 0; i < bins.length; i++) {
        dataArray.push([i.toString(), bins[i]]);
    }

    const data = google.visualization.arrayToDataTable(dataArray);

    const options = {
        chartArea: {
            left: 40,
            right: 20,
            top: 20,
            bottom: 60,
            width: "90%",
            height: "75%",
        },
        hAxis: {
            title: "",
            gridlines: { count: 11 },
        },
        legend: "none",
        width: "100%",
        height: 400,
    };

    const chart = new google.visualization.ColumnChart(document.getElementById(containerId));
    chart.draw(data, options);
	
    if (!window.testResizeListenerAdded) {
        window.addEventListener("resize", () => {
            chart.draw(data, options);
        });
        window.testResizeListenerAdded = true;
    }
	
}

function drawPieChart(containerId, dataArray) {
	
  var data = google.visualization.arrayToDataTable(dataArray);
  
  var options = {
    pieHole: 0,
    width: 400,
    height: 300,
    title: ''
  };
  
  const chart = new google.visualization.PieChart(document.getElementById(containerId));
  chart.draw(data, options);
  
}

