DROP ALL OBJECTS;

CREATE TABLE IF NOT EXISTS films (
    id long generated by default as identity primary key,
    mpa_id long not null,
    name varchar(255) not null,
    description text,
    release_date date,
    duration int not null default 0
);

CREATE TABLE IF NOT EXISTS mpas (
    id long generated by default as identity primary key,
    name varchar(255) not null
);

CREATE TABLE IF NOT EXISTS genres (
    id long generated by default as identity primary key,
    name varchar(255) not null
);

CREATE TABLE IF NOT EXISTS users (
    id long generated by default as identity primary key,
    email varchar(255) not null unique,
    login varchar(255) not null unique,
    name varchar(255) not null,
    birthday date
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id long,
    genre_id long,
    foreign key (film_id) references films (id) on DELETE cascade,
    foreign key (genre_id) references genres (id) on DELETE cascade,
    primary key (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS film_like (
    film_id long,
    user_id long,
    foreign key (film_id) references films (id) on DELETE cascade,
    foreign key (user_id) references users (id) on DELETE cascade,
    primary key (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friendship (
    user_id long,
    friend_id long,
    active boolean not null default false,
    foreign key (user_id) references users (id) on DELETE cascade,
    foreign key (friend_id) references users (id) on DELETE cascade,
    primary key (user_id, friend_id)
);