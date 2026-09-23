function renderizarGraficoEvolucao() {
    var data = window.dashboardData;

    // Proteção: Se não houver dados, encerra a função silenciosamente
    if (!data || !data.rotulos || data.rotulos.length === 0) return;

    var options = {
        chart: {
            type: 'line',
            height: 280,
            toolbar: { show: false },
            fontFamily: 'inherit',
            zoom: { enabled: false }
        },
        series: [
            { name: 'Receitas', data: data.receitas },
            { name: 'Despesas', data: data.despesas }
        ],
        colors: ['#2fb344', '#f59f00'],
        stroke: { width: 3, curve: 'smooth' },
        markers: { size: 4, hover: { size: 6 } },
        xaxis: {
            categories: data.rotulos,
            axisBorder: { show: false },
            axisTicks: { show: false }
        },
        yaxis: {
            labels: {
                formatter: function (val) { return val ? val.toLocaleString('pt-BR') : '0'; }
            }
        },
        grid: { strokeDashArray: 4, borderColor: '#eef1f4' },
        dataLabels: { enabled: false },
        legend: {
            position: 'bottom',
            horizontalAlign: 'center',
            markers: { width: 10, height: 10, radius: 12 }
        }
    };

    var chart = new ApexCharts(document.querySelector("#graficoEvolucao"), options);
    chart.render();
}

// Lógica Sênior: Verifica o status do documento para garantir que o gráfico seja desenhado
if (document.readyState === 'loading') {
    document.addEventListener("DOMContentLoaded", renderizarGraficoEvolucao);
} else {
    // Se o Thymeleaf injetou o script após o DOMContentLoaded, desenha direto
    renderizarGraficoEvolucao();
}
