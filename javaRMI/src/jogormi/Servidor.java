/*
Modifique o "jogo" desenvolvido na última aula (14/05/2019 - Jogo RMI Callback), de modo que:
&)Todos os clientes apresentem em sua tela a informação atualizada de quantos jogadores estão conectados;

&)Não permitir que um jogador, ao entrar no jogo, comece na mesma posição de outro;

OK)Como cada jogador possui um código de 0 a 9, o jogo não deve permitir que hajam mais do que 10 jogadores ativos;

&)Faça com que o servidor, de tempos em tempos, adicione ao tabuleiro uma "moeda", que, quando um jogador passa em cima, ela é coletada. Você pode escolher entre o servidor ficar adicionando moedas o tempo inteiro ou somente uma moeda por vez (só adicionar uma nova quando a última for "pega"). Adicione a informação sobre a quantidade de moedas na tela. Faça com que o primeiro jogador a alcançar 10 moedas vença.

&)Quando um jogador sai, o seu código fica "livre", porém, quando um novo jogador entra, ele sempre recebe um código referente a ao valor de um contador (por exemplo, os clientes 0, 1 e 2 estão online, se o cliente 1 sair e um cliente novo entrar, o novo cliente receberá código 3 e não 1). Modifique isso de forma que os novos jogadores sempre recebam o código de menor valor possível que esteja livre (ou seja, no exemplo anterior, o novo cliente receberá o código 1).

&)(Extra) Faça o jogo utilizando Java 2D ao invés de um jTextArea, ou seja, deixe o jogo mais bonitinho :) Quanto maior a criatividade, maior a nota extra :D

&)(Extra) Toda funcionalidade/jogabilidade extra também será considerada!
*/


package jogormi;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Servidor extends UnicastRemoteObject implements IServidor {

    int codigoAtual = 0;
    ArrayList<ICliente> listaClientes = new ArrayList<>();
    ArrayList<ICliente> listaMoedas = new ArrayList<>();
    ArrayList<ClientePosicao> listaPosicoes = new ArrayList<>();

    @Override
    public void meTiraDaLista(int codigo, ICliente cli) throws RemoteException {
        listaClientes.remove(cli);
        for (ClientePosicao p : listaPosicoes) {
            if(p.codigo == codigo){
                listaPosicoes.remove(p);
                break;
            }
        }
        for (ICliente cli2 : listaClientes) {
            cli2.liberaPosicao(codigo);
        }
    }

    class ClientePosicao {

        int linha, coluna, codigo;
    }

    public Servidor() throws RemoteException {

    }

    @Override
    public int registraCliente(ICliente cli) throws RemoteException {
        if(listaClientes.size() < 3){
            listaClientes.add(cli);
            for (ClientePosicao p2 : listaPosicoes) {
                cli.recebePosicao(p2.linha, p2.coluna, p2.codigo);
            }
            ClientePosicao p = new ClientePosicao();
            codigoAtual++;
            p.codigo = codigoAtual;
            listaPosicoes.add(p);
            return p.codigo;
        
        }else{
            System.out.println("esta cheia");  
            return -1; 
        }
    }

    @Override
    public void enviaPosicao(int linha, int coluna, int codigo) throws RemoteException {
        //atualiza a posição do cliente na lista de posições
        System.out.println("Lista atualizada:");
        for (int i = 0; i < listaPosicoes.size(); i++) {
            ClientePosicao p = listaPosicoes.get(i);
            if (p.codigo == codigo) {
                p.linha = linha;
                p.coluna = coluna;
            }
            System.out.println(p.codigo+": "+p.linha+","+p.coluna);
        }
        for (ICliente cli : listaClientes) {
            cli.recebePosicao(linha, coluna, codigo);
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            Servidor s = new Servidor();
            Naming.bind("rmi://localhost/Servidor", s);
        } catch (AlreadyBoundException e) {
            System.out.println("Objeto já registrado");
        } catch (RemoteException ex) {
            System.out.println("Erro de comunicação");
        } catch (MalformedURLException ex) {
            System.out.println("URL mal formada");
        }
    }
}
