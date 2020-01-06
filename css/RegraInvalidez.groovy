package com.inova10.simulador.calculos

import com.inova10.simulador.IntervaloTrabalho
import com.inova10.simulador.IntervaloTempo
import com.inova10.simulador.calculos.FiltroTempo
import java.text.SimpleDateFormat
import com.inova10.simulador.IntervaloAfastamento
import com.inova10.simulador.Trabalhador

class RegraInvalidez extends RegraAposentadoria{
    Date ingressoNoCargoAtual
    List<IntervaloTempo> intervalosTempo
    Boolean podeImprimir
    Boolean impossivel
    Boolean entrouNoServicoPublicoAntes31_12_2003
	  Boolean estaNoServicoPublicoEm31_12_2003
    Integer licencaPremio
    int anos
    int meses
    int dias

    RegraInvalidez(String descricao, List<IntervaloTempo> intervalosTempo, Trabalhador trabalhador){
        this.impossivel = false;
        this.podeImprimir = true;
        this.intervalosTempo = intervalosTempo;
        this.invalidez = trabalhador.getInvalidez();
        this.descricao = descricao;
		    this.licencaPremio = trabalhador.getLicencaPremio();
    }

	RegraInvalidez(){}

    public void calcula(){
  		if(!this.invalidez)return
  		this.invalidez = this.invalidez - 1
      FiltroTempo filtro = FiltroTempo.instance;
      ContadorDias contaDias = new ContadorDias()
      List<IntervaloTrabalho> trabalho = filtro.apenasIntervalosTrabalho(this.intervalosTempo);
	    List<IntervaloAfastamento> afastamentos = filtro.apenasAfastamentos(this.intervalosTempo);
      List<IntervaloTrabalho> tempoCargo = filtro.apenasTempoNoCargo(trabalho)
      if(!tempoCargo.empty){
          tempoCargo.sort();
          this.ingressoNoCargoAtual = tempoCargo.last().inicio;
      }
  		//Intervalos de trabalho e afastamentos antes da invalidez
  		List<IntervaloTrabalho> trabalhoAntesDaInvalidez = filtro.getIntervalosTempoAteLimite(trabalho, this.invalidez)
  		List<IntervaloAfastamento> afastamentosAntesDaInvalidez = filtro.getIntervalosTempoAteLimite(afastamentos, this.invalidez)
      //separa intervalos do servico publico
      List<IntervaloTrabalho> intervalosServicoPublico  = filtro.apenasTempoServicoPublico(trabalhoAntesDaInvalidez);

      //soma quantidade de dias trabalhados antes da data de invalidez
      int totalDiasAntesDataInvalidez=contaDias.conta(trabalhoAntesDaInvalidez)
      //soma dias de afastamento nao contribuidos para subtrair do total de dias
      int diasAfastamentoParaDeduzir=contaDias.contaAfastamentosNaoContribuidos(afastamentosAntesDaInvalidez);

      //dias validos para calculo de dia,mes e ano
      int tempoApuradoValido=totalDiasAntesDataInvalidez+licencaPremio-diasAfastamentoParaDeduzir

      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
  		Date dataAlvo31_12_2003 = sdf.parse("31/12/2003")
      if(!intervalosServicoPublico.empty){
          if(intervalosServicoPublico.sort().first().inicio.before(dataAlvo31_12_2003)){
              entrouNoServicoPublicoAntes31_12_2003=true;
          }else{
              entrouNoServicoPublicoAntes31_12_2003=false;
          }
      }
  		calculaAnosMesesDias(tempoApuradoValido);
      this.possivel = this.invalidez;
      this.impossivel = (this.invalidez < this.ingressoNoCargoAtual);

  		CondicaoEstarServicoPublicoData estarNoServicoPublicoData = new CondicaoEstarServicoPublicoData(intervalos: trabalhoAntesDaInvalidez, dataAlvo: dataAlvo31_12_2003);
  		estarNoServicoPublicoData.calcula();
  		estaNoServicoPublicoEm31_12_2003 = estarNoServicoPublicoData.possivel ? true:false
      Date dia12_11_2019 = sdf.parse("12/11/2019");
      if(!impossivel){
        this.podeImprimir = (this.invalidez <= dia12_11_2019)
      }
    }

    public void calculaAnosMesesDias(int dias){

        if (dias < 365) {
            if (dias < 30) {
                this.anos=0;
                this.meses = 0;
                this.dias = dias;
            } else {
                this.anos = 0;
                this.meses = dias / 30;
                this.dias = dias % 30;
            }
        } else {
            this.anos = dias / 365;
            int aux = 0;
            aux = dias % 365;
            if (aux < 30) {
                this.meses = 0;
                this.dias = aux;
            } else {
                this. meses = aux / 30;
                this.dias = aux % 30;
            }
        }

    }
}
