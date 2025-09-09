Chess Game com Java e Ia
Descrição
Este é um jogo de xadrez funcional desenvolvido em Java, utilizando a biblioteca Swing para a interface gráfica. O projeto permite partidas tanto para dois jogadores locais (Player vs. Player) quanto para um jogador contra uma Inteligência Artificial (Player vs. AI) com múltiplos níveis de dificuldade.

O desenvolvimento da IA foi inspirado e guiado por conceitos de design de motores de xadrez, desde algoritmos básicos até o clássico Minimax para tomada de decisão estratégica.
Funcionalidades
Interface Gráfica Completa: Tabuleiro de xadrez interativo criado com Java Swing.
Dois Modos de Jogo:
Jogador vs. Jogador: Permite que dois jogadores disputem uma partida no mesmo computador.
Jogador vs. IA: Jogue contra um oponente controlado pelo computador. (IA com Níveis de Dificuldade:)

Fácil: A IA seleciona os seus movimentos de forma aleatória.
Médio: A IA avalia as posições um movimento à frente, escolhendo a jogada que resulta na melhor vantagem material imediata.
Difícil: A IA utiliza o algoritmo Minimax para analisar as jogadas com vários níveis de profundidade, permitindo-lhe antecipar as respostas do oponente e planear com mais estratégia.

Regras de Xadrez Implementadas:
Movimentação básica de todas as peças.
Captura de peças.
Promoção de peões.
Histórico de Jogadas: Um painel lateral exibe o histórico de todos os movimentos realizados durante a partida.
Estrutura do Projeto
O código está organizado em três pacotes principais, seguindo o padrão Model-View-Controller (MVC):
model: Contém a lógica de negócio e as regras do jogo.

view: Responsável por toda a interface gráfica (GUI).

ChessGUI: A janela principal do jogo, que renderiza o tabuleiro e interage com o utilizador.

ImageUtil: Classe utilitária para carregar as imagens das peças.

controller: Faz a ligação entre o model e a view.

Game: Orquestra o estado do jogo (de quem é a vez, histórico, fim de jogo).

AIPlayer: Contém toda a lógica da Inteligência Artificial.

AIDifficulty: Enum para definir os níveis de dificuldade.

Como Executar o Projeto
Pré-requisitos:

É necessário ter o JDK (Java Development Kit) 11 ou superior instalado.

Compilação e Execução:

Abra o projeto no seu IDE (Eclipse, IntelliJ, VS Code, etc.).

Encontre o ficheiro src/view/ChessGUI.java.

Execute o método main contido neste ficheiro.

Início do Jogo:
Ao executar, um menu irá aparecer para selecionar o modo de jogo (Jogador vs. Jogador ou Jogador vs. IA).
Se o modo IA for selecionado, um segundo menu aparecerá para escolher a dificuldade (Fácil, Médio ou Difícil).
Após a seleção, o tabuleiro de xadrez será exibido e o jogo começará.

Esse projeto tem intuito inteiramente universitario, visando aplicar conhecimentos obtidos na aula
Implementar Regras Especiais: Adicionar a lógica para movimentos como o roque, a captura en passant e a deteção de xeque e xeque-mate.



Destacar a última jogada realizada no tabuleiro.

Adicionar a funcionalidade de salvar e carregar jogos.
