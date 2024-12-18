CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY, -- Уникальное имя пользователя
    password_hash VARCHAR(255) NOT NULL, -- Хэш пароля
    games_played INT DEFAULT 0,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    draws INT DEFAULT 0
);

CREATE TABLE games (
    id INT AUTO_INCREMENT PRIMARY KEY, -- Уникальный идентификатор игры
    username VARCHAR(50) NOT NULL, -- Имя пользователя
    player_symbol CHAR(1) NOT NULL CHECK (player_symbol IN ('X', 'O')), -- Символ игрока ('X' или 'O')
    board_state TEXT NOT NULL, -- Текущее состояние игры в виде строки
    is_finished BOOLEAN DEFAULT FALSE, -- Статус игры: завершена или нет
    winner ENUM('user', 'server', 'draw') DEFAULT NULL, -- Победитель
    game_date VARCHAR(50) DEFAULT '', -- Дата в текстовом формате
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE -- Удаление игр при удалении пользователя
);

ALTER TABLE users
ADD COLUMN salt VARCHAR(255) NOT NULL;
