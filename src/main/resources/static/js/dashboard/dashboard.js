document.addEventListener("DOMContentLoaded", function () {
    const competencia = document.getElementById("competenciaAtiva").value;

    // Dispara todas as requisições em paralelo. Um erro num card não afeta o outro.
    carregarFragmento(`/dashboards/fragmentos/saldo-consolidado`, "container-card1");
    carregarFragmento(`/dashboards/fragmentos/saldo-conta`, "container-card2");
    carregarFragmento(`/dashboards/fragmentos/limite-cartao`, "container-card7");

    carregarFragmento(`/dashboards/fragmentos/receitas-despesas?competencia=${competencia}`, "container-card3");
    carregarFragmento(`/dashboards/fragmentos/status-pagamento?competencia=${competencia}`, "container-card5");

    carregarFragmento(`/dashboards/fragmentos/despesas-categoria?competencia=${competencia}`, "container-card4");
    carregarFragmento(`/dashboards/fragmentos/rateio-contato?competencia=${competencia}`, "container-card6");

    // Passa a função de renderizar o gráfico como callback, para executar só depois do HTML chegar
    carregarFragmento(`/dashboards/fragmentos/evolucao?competencia=${competencia}`, "container-card8", renderizarGraficoEvolucao);
});

/**
 * Função utilitária Sênior: Faz a requisição Fetch, injeta o HTML e força a execução de scripts.
 */
function carregarFragmento(url, containerId, callbackSucesso = null) {
    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error(`Erro na requisição: HTTP ${response.status}`);
            return response.text(); // Pega o HTML retornado pelo Spring
        })
        .then(html => {
            const container = document.getElementById(containerId);
            container.innerHTML = html;

            // Hack Sênior: O navegador não executa scripts injetados via innerHTML por segurança.
            // Precisamos clonar a tag script que veio no Thymeleaf e reinjetar no DOM.
            Array.from(container.querySelectorAll("script")).forEach(oldScript => {
                const newScript = document.createElement("script");
                Array.from(oldScript.attributes).forEach(attr => newScript.setAttribute(attr.name, attr.value));
                newScript.appendChild(document.createTextNode(oldScript.innerHTML));
                oldScript.parentNode.replaceChild(newScript, oldScript);
            });

            // Se for o card 8, chama a função do gráfico
            if (callbackSucesso) callbackSucesso();
        })
        .catch(error => {
            console.error(`Falha ao carregar o componente ${containerId}:`, error);
            document.getElementById(containerId).innerHTML = `
                <div class="card h-100 d-flex align-items-center justify-content-center p-4">
                    <span class="text-danger"><i class="ph ph-warning me-2"></i> Indisponível</span>
                </div>`;
        });
}

/**
 * Desenha o gráfico lendo a variável global que o fragmento injetou.
 */
function renderizarGraficoEvolucao() {
    var data = window.dadosEvolucao; // Variável criada pelo Card 8

    if (!data || !data.rotulos || data.rotulos.length === 0) return;

    var chartContainer = document.querySelector("#graficoEvolucao");
    if (!chartContainer) return;

    chartContainer.innerHTML = ''; // Limpa gráfico antigo

    var options = {
        chart: { type: 'line', height: 280, toolbar: { show: false }, fontFamily: 'inherit', zoom: { enabled: false } },
        series: [
            { name: 'Receitas', data: data.receitas },
            { name: 'Despesas', data: data.despesas }
        ],
        colors: ['#2fb344', '#f59f00'],
        stroke: { width: 3, curve: 'smooth' },
        markers: { size: 4, hover: { size: 6 } },
        xaxis: { categories: data.rotulos, axisBorder: { show: false }, axisTicks: { show: false } },
        yaxis: {
            labels: { formatter: function (val) { return val ? val.toLocaleString('pt-BR') : '0'; } }
        },
        grid: { strokeDashArray: 4, borderColor: '#eef1f4' },
        dataLabels: { enabled: false },
        legend: { position: 'bottom', horizontalAlign: 'center', markers: { width: 10, height: 10, radius: 12 } }
    };

    var chart = new ApexCharts(chartContainer, options);
    chart.render();
}