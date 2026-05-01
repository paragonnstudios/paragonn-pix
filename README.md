# 🚀 Paragonn Pix

<p align="center">
  <strong>Pagamento via PIX totalmente integrado ao Minecraft</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-Spigot-green?style=for-the-badge">
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge">
  <img src="https://img.shields.io/badge/License-Open%20Source-blue?style=for-the-badge">
  <img src="https://img.shields.io/badge/API-MercadoPago-yellow?style=for-the-badge">
</p>

---

## 🌌 Sobre o Projeto

O **Paragonn Pix** é um plugin avançado para servidores Spigot que permite a integração direta de pagamentos via **PIX** dentro do Minecraft, oferecendo uma experiência moderna, rápida e totalmente automatizada para monetização de servidores.

Com ele, jogadores podem adquirir produtos digitais como VIPs, moedas, itens e permissões utilizando QR Code PIX gerado em tempo real — tudo sem sair do jogo.

A proposta do Paragonn Pix é simples: **transformar o processo de compra em algo fluido, seguro e instantâneo**, elevando o nível de profissionalismo do seu servidor.

---

## ⚙️ Como Funciona

O fluxo de compra foi projetado para ser intuitivo:

1. O jogador executa o comando `/comprarpix`
2. Um menu interativo é exibido com os produtos disponíveis
3. Ao selecionar um produto, um QR Code PIX é gerado automaticamente
4. O jogador realiza o pagamento pelo aplicativo do banco
5. O sistema valida a transação
6. A recompensa é entregue automaticamente

Esse processo pode levar apenas alguns segundos dependendo do modo de validação configurado.

---

## 🧠 Modos de Validação

### 🔄 Modo Automático

Neste modo, o sistema realiza verificações periódicas nas transações pendentes utilizando a API do MercadoPago.

* Confirmação automática do pagamento
* Experiência totalmente hands-free para o jogador
* Intervalo de verificação configurável

⚠️ Observação:
Intervalos muito curtos podem aumentar o consumo de recursos do servidor devido à quantidade de requisições.

---

### ✍️ Modo Manual

Após realizar o pagamento, o jogador pode validar manualmente utilizando:

```
/pix validar <codigo>
```

* Sem taxas intermediárias
* Utiliza código E2E do PIX
* Mais controle, porém menos automático

⚠️ Pode apresentar limitações em ambientes com alto volume de transações.

---

## 🎮 Comandos

### 👤 Jogadores

* `/comprarpix` → Abre o menu principal de compras
* `/comprarpix <inventario>` → Abre um menu específico
* `/pix validar <codigo>` → Valida um pagamento manual
* `/pix lista` → Lista pedidos do jogador
* `/pix info` → Exibe instruções detalhadas
* `/produto <produto>` → Exibe o menu de confirmação direta do produto

### 🛠️ Administradores

* `/pix lista <jogador>` → Lista pedidos de outro jogador
* `/pix reload` → Recarrega configurações

---

## 🔐 Permissões

* `paragonnpix.use` → Permite utilizar o sistema de compras
* `paragonnpix.admin` → Permite acesso administrativo

---

## ✨ Recursos

* Integração com MercadoPago
* Geração de QR Code em tempo real
* Sistema automático de entrega de recompensas
* Suporte a múltiplos menus de loja
* Proteção contra spam e sobrecarga
* Mensagens 100% configuráveis
* Suporte a banco de dados
* Alta escalabilidade

---

## 🧩 Casos de Uso

* Venda de VIPs
* Sistemas de cash
* Itens exclusivos
* Liberação de áreas ou perks

---

## 📦 Instalação

1. Baixe a versão mais recente na aba de releases
2. Coloque o arquivo `.jar` na pasta `plugins`
3. Inicie o servidor
4. Configure o arquivo `config.yml`

    * Token do MercadoPago
    * Chave PIX
    * Banco de dados
5. Reinicie o servidor

---

## 🛡️ Segurança

O sistema foi desenvolvido com foco em segurança:

* Validação via API oficial
* Controle de requisições
* Proteção contra duplicidade

---

## 📹 Demonstrações

* [https://youtu.be/vVs14RqBq3Q](https://youtu.be/vVs14RqBq3Q)
* [https://youtu.be/38rZIy0lXbM](https://youtu.be/38rZIy0lXbM)

---

## 🌍 Open Source

O Paragonn Pix é um projeto open source.

Sinta-se livre para contribuir, melhorar o código, sugerir novas funcionalidades ou adaptar o sistema para seu servidor.

---

## 🙏 Créditos

Agradecimentos especiais a **@rapust** pelo projeto base utilizado para geração de QR Codes:

🔗 [https://github.com/rapust/QRCodeMap](https://github.com/rapust/QRCodeMap)

---

## 📜 Licença

Este projeto está disponível sob uma licença open source. Consulte o repositório para mais detalhes.

---

<p align="center">
  Desenvolvido para elevar o nível dos servidores Minecraft 🚀
</p>