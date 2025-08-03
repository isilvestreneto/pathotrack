# 🦆 pathotrack - Gerenciador Pessoal de Casos Patológicos

**Aplicativo Android offline** desenvolvido em Java para uso exclusivo de médicos patologistas. O objetivo é oferecer uma ferramenta prática e acessível para registrar, consultar e acompanhar a evolução de casos clínicos sob responsabilidade do profissional, diretamente do smartphone.

---

## 🩺 Objetivo

Facilitar o controle dos casos que passam pelas etapas laboratoriais da anatomia patológica, desde o recebimento até a emissão do laudo. O aplicativo funciona 100% offline, ideal para uso em ambientes hospitalares, laboratórios ou durante deslocamentos.

---

## 🧠 Funcionalidades

- Cadastro de novos casos com:
  - Data e hora da requisição
  - Previsão de entrega
  - Número do exame
  - Dados do paciente (nome, data de nascimento, idade, sexo, SUS, prontuário)

- Acompanhamento por etapas:
  - Recebimento  
  - Macroscopia  
  - Processamento  
  - Corte histológico  
  - Laudo  

- Filtros e ordenações na tela inicial:
  - Por vencimento (padrão)
  - Por nome do paciente
  - Por data de entrada
  - Apenas casos vencidos ou próximos do vencimento

---

## 🧱 Tecnologias Utilizadas

- **Linguagem:** Java  
- **IDE:** Android Studio  
- **Plataforma:** Android (SDK 24+)  
- **Persistência local:** SQLite ou Room (a definir)  
- **Arquitetura:** MVVM (recomendado)

---

## 🗃️ Estrutura de Entidades

### Paciente
- `id`
- `nome`
- `dataNascimento`
- `idade`
- `sexo`
- `sus`
- `prontuario`

### Caso
- `id`
- `pacienteId` (chave estrangeira)
- `numeroExame`
- `dataRequisicao` (data e hora)
- `previsaoEntrega` (data)
- `etapaAtual` (enum)

---

## 🚧 Status do Projeto

🚀 Em desenvolvimento inicial  
✅ Funcionalidades mínimas planejadas  
📱 Foco em usabilidade offline

---

## 📌 Observações

Este aplicativo é voltado exclusivamente para uso pessoal do médico patologista. Ele não realiza integração com sistemas hospitalares, não possui acesso em rede e não compartilha dados com terceiros.

---

## 📄 Licença

Este projeto é de uso acadêmico. Consulte o autor para fins de reutilização ou adaptação.
