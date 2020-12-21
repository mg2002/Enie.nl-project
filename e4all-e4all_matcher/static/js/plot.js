var data = [demand, supply, transactions];

var layout = {
  title: 'Demand and Supply',
  xaxis: {tickfont: {
      size: 14, 
      color: 'rgb(107, 107, 107)'
    }}, 
  yaxis: {
    title: 'Energy',
    titlefont: {
      size: 16, 
      color: 'rgb(107, 107, 107)'
    }, 
    tickfont: {
      size: 14, 
      color: "rgb(107, 107, 107)"
    }
  }, 
  legend: {
    x: 0, 
    y: 1.0, 
    bgcolor: 'rgba(255, 255, 255, 0)',
    bordercolor: 'rgba(255, 255, 255, 0)'
  }, 
  barmode: 'group', 
  bargap: 0.15, 
  bargroupgap: 0.1
};

Plotly.newPlot('myDiv', data, layout);