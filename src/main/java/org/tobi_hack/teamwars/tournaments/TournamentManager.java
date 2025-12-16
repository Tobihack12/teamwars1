package org.tobi_hack.teamwars.tournaments;

import org.tobi_hack.teamwars.Main;
import org.tobi_hack.teamwars.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TournamentManager {

    private final Main plugin;
    private final Map<String, Tournament> tournaments = new HashMap<>();
    private Tournament activeTournament = null;
    private File tournamentsFile;
    private YamlConfiguration tournamentsConfig;

    public TournamentManager(Main plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        tournamentsFile = new File(plugin.getDataFolder(), "tournaments.yml");
        if (!tournamentsFile.exists()) {
            try {
                tournamentsFile.createNewFile();
                createDefaultTournaments();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creando tournaments.yml: " + e.getMessage());
            }
        }

        tournamentsConfig = YamlConfiguration.loadConfiguration(tournamentsFile);
        loadTournaments();
    }

    // ==================== CLASE DE TORNEO ====================

    public static class Tournament {
        private final String id;
        private String name;
        private String description;
        private TournamentType type;
        private int maxTeams;
        private int minTeams;
        private int teamSize;
        private TournamentStatus status;
        private List<Team> participants;
        private Map<Integer, TournamentRound> rounds;
        private Team winner;
        private long startTime;
        private long endTime;
        private Map<String, Object> rewards;
        private Map<String, Object> settings;

        public enum TournamentType {
            SINGLE_ELIMINATION("Eliminación Simple"),
            DOUBLE_ELIMINATION("Doble Eliminación"),
            ROUND_ROBIN("Liga Todos contra Todos"),
            SWISS("Sistema Suizo"),
            CUSTOM("Personalizado");

            private final String displayName;

            TournamentType(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }

        public enum TournamentStatus {
            ANNOUNCED("Anunciado"),
            REGISTRATION_OPEN("Inscripciones Abiertas"),
            REGISTRATION_CLOSED("Inscripciones Cerradas"),
            IN_PROGRESS("En Progreso"),
            FINISHED("Finalizado"),
            CANCELLED("Cancelado");

            private final String displayName;

            TournamentStatus(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }

        public Tournament(String id, String name, TournamentType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.status = TournamentStatus.ANNOUNCED;
            this.participants = new ArrayList<>();
            this.rounds = new HashMap<>();
            this.settings = new HashMap<>();
            this.rewards = new HashMap<>();

            // Configuración por defecto
            this.maxTeams = 16;
            this.minTeams = 4;
            this.teamSize = 4;
            this.startTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // Mañana
        }

        // Getters y Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public TournamentType getType() { return type; }
        public void setType(TournamentType type) { this.type = type; }
        public int getMaxTeams() { return maxTeams; }
        public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }
        public int getMinTeams() { return minTeams; }
        public void setMinTeams(int minTeams) { this.minTeams = minTeams; }
        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
        public TournamentStatus getStatus() { return status; }
        public void setStatus(TournamentStatus status) { this.status = status; }
        public List<Team> getParticipants() { return new ArrayList<>(participants); }
        public void addParticipant(Team team) {
            if (participants.size() < maxTeams && !participants.contains(team)) {
                participants.add(team);
            }
        }
        public void removeParticipant(Team team) { participants.remove(team); }
        public Map<Integer, TournamentRound> getRounds() { return new HashMap<>(rounds); }
        public Team getWinner() { return winner; }
        public void setWinner(Team winner) { this.winner = winner; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public Map<String, Object> getRewards() { return new HashMap<>(rewards); }
        public void setRewards(Map<String, Object> rewards) { this.rewards = rewards; }
        public Map<String, Object> getSettings() { return new HashMap<>(settings); }
        public void setSetting(String key, Object value) { settings.put(key, value); }

        public boolean canRegister() {
            return status == TournamentStatus.REGISTRATION_OPEN &&
                    participants.size() < maxTeams;
        }

        public int getParticipantCount() {
            return participants.size();
        }

        public String getFormattedStartTime() {
            long timeLeft = startTime - System.currentTimeMillis();
            if (timeLeft <= 0) return "¡Ahora!";

            long hours = timeLeft / (60 * 60 * 1000);
            long minutes = (timeLeft % (60 * 60 * 1000)) / (60 * 1000);

            return String.format("%02d:%02d", hours, minutes);
        }

        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("description", description);
            data.put("type", type.name());
            data.put("maxTeams", maxTeams);
            data.put("minTeams", minTeams);
            data.put("teamSize", teamSize);
            data.put("status", status.name());
            data.put("startTime", startTime);
            data.put("endTime", endTime);
            data.put("winner", winner != null ? winner.getTeamId().toString() : null);
            data.put("rewards", rewards);
            data.put("settings", settings);

            // Serializar participantes
            List<String> participantIds = participants.stream()
                    .map(team -> team.getTeamId().toString())
                    .collect(Collectors.toList());
            data.put("participants", participantIds);

            // Serializar rondas
            Map<String, Object> roundsData = new HashMap<>();
            for (Map.Entry<Integer, TournamentRound> entry : rounds.entrySet()) {
                roundsData.put(String.valueOf(entry.getKey()), entry.getValue().serialize());
            }
            data.put("rounds", roundsData);

            return data;
        }

        public void deserialize(Map<String, Object> data) {
            name = (String) data.get("name");
            description = (String) data.get("description");
            type = TournamentType.valueOf((String) data.get("type"));
            maxTeams = (int) data.get("maxTeams");
            minTeams = (int) data.get("minTeams");
            teamSize = (int) data.get("teamSize");
            status = TournamentStatus.valueOf((String) data.get("status"));
            startTime = (long) data.get("startTime");
            endTime = (long) data.getOrDefault("endTime", 0L);
            rewards = (Map<String, Object>) data.getOrDefault("rewards", new HashMap<>());
            settings = (Map<String, Object>) data.getOrDefault("settings", new HashMap<>());

            // Cargar ganador
            String winnerId = (String) data.get("winner");
            if (winnerId != null) {
                // TODO: Cargar equipo por ID
            }

            // Cargar participantes
            participants.clear();
            List<String> participantIds = (List<String>) data.get("participants");
            if (participantIds != null) {
                for (String teamId : participantIds) {
                    // TODO: Cargar equipo por ID
                }
            }

            // Cargar rondas
            rounds.clear();
            Map<String, Object> roundsData = (Map<String, Object>) data.get("rounds");
            if (roundsData != null) {
                for (Map.Entry<String, Object> entry : roundsData.entrySet()) {
                    int roundNum = Integer.parseInt(entry.getKey());
                    TournamentRound round = new TournamentRound(roundNum);
                    round.deserialize((Map<String, Object>) entry.getValue());
                    rounds.put(roundNum, round);
                }
            }
        }
    }

    // ==================== CLASE DE RONDA ====================

    public static class TournamentRound {
        private final int roundNumber;
        private List<TournamentMatch> matches;
        private boolean completed;

        public TournamentRound(int roundNumber) {
            this.roundNumber = roundNumber;
            this.matches = new ArrayList<>();
            this.completed = false;
        }

        public int getRoundNumber() { return roundNumber; }
        public List<TournamentMatch> getMatches() { return new ArrayList<>(matches); }
        public void addMatch(TournamentMatch match) { matches.add(match); }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("roundNumber", roundNumber);
            data.put("completed", completed);

            List<Map<String, Object>> matchesData = matches.stream()
                    .map(TournamentMatch::serialize)
                    .collect(Collectors.toList());
            data.put("matches", matchesData);

            return data;
        }

        public void deserialize(Map<String, Object> data) {
            completed = (boolean) data.get("completed");

            matches.clear();
            List<Map<String, Object>> matchesData = (List<Map<String, Object>>) data.get("matches");
            if (matchesData != null) {
                for (Map<String, Object> matchData : matchesData) {
                    TournamentMatch match = new TournamentMatch();
                    match.deserialize(matchData);
                    matches.add(match);
                }
            }
        }
    }

    // ==================== CLASE DE PARTIDO ====================

    public static class TournamentMatch {
        private Team team1;
        private Team team2;
        private Team winner;
        private MatchStatus status;
        private Map<String, Object> scores;
        private long startTime;
        private long endTime;

        public enum MatchStatus {
            SCHEDULED("Programado"),
            IN_PROGRESS("En Progreso"),
            TEAM1_WIN("Ganó Equipo 1"),
            TEAM2_WIN("Ganó Equipo 2"),
            DRAW("Empate"),
            CANCELLED("Cancelado");

            private final String displayName;

            MatchStatus(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }

        public TournamentMatch() {
            this.status = MatchStatus.SCHEDULED;
            this.scores = new HashMap<>();
        }

        public TournamentMatch(Team team1, Team team2) {
            this();
            this.team1 = team1;
            this.team2 = team2;
        }

        // Getters y Setters
        public Team getTeam1() { return team1; }
        public void setTeam1(Team team1) { this.team1 = team1; }
        public Team getTeam2() { return team2; }
        public void setTeam2(Team team2) { this.team2 = team2; }
        public Team getWinner() { return winner; }
        public void setWinner(Team winner) { this.winner = winner; }
        public MatchStatus getStatus() { return status; }
        public void setStatus(MatchStatus status) { this.status = status; }
        public Map<String, Object> getScores() { return new HashMap<>(scores); }
        public void setScore(String key, Object value) { scores.put(key, value); }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }

        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("team1", team1 != null ? team1.getTeamId().toString() : null);
            data.put("team2", team2 != null ? team2.getTeamId().toString() : null);
            data.put("winner", winner != null ? winner.getTeamId().toString() : null);
            data.put("status", status.name());
            data.put("scores", scores);
            data.put("startTime", startTime);
            data.put("endTime", endTime);
            return data;
        }

        public void deserialize(Map<String, Object> data) {
            // TODO: Cargar equipos por ID
            status = MatchStatus.valueOf((String) data.get("status"));
            scores = (Map<String, Object>) data.getOrDefault("scores", new HashMap<>());
            startTime = (long) data.getOrDefault("startTime", 0L);
            endTime = (long) data.getOrDefault("endTime", 0L);
        }
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    private void createDefaultTournaments() {
        // Torneo de inicio rápido
        Tournament quickTournament = new Tournament("weekly", "Torneo Semanal Rápido",
                Tournament.TournamentType.SINGLE_ELIMINATION);
        quickTournament.setDescription("¡Torneo semanal de TeamWars! Inscripción gratuita.");
        quickTournament.setMaxTeams(8);
        quickTournament.setMinTeams(4);
        quickTournament.setTeamSize(4);
        quickTournament.setStartTime(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)); // 1 semana

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("first", Arrays.asList("DIAMOND_BLOCK:1", "ENCHANTED_GOLDEN_APPLE:5"));
        rewards.put("second", Arrays.asList("GOLD_BLOCK:1", "GOLDEN_APPLE:10"));
        rewards.put("third", Arrays.asList("IRON_BLOCK:2", "DIAMOND:5"));
        quickTournament.setRewards(rewards);

        tournaments.put("weekly", quickTournament);

        // Torneo mensual premium
        Tournament monthlyTournament = new Tournament("monthly", "Copa Mensual Élite",
                Tournament.TournamentType.DOUBLE_ELIMINATION);
        monthlyTournament.setDescription("Torneo mensual premium con grandes recompensas.");
        monthlyTournament.setMaxTeams(16);
        monthlyTournament.setMinTeams(8);
        monthlyTournament.setTeamSize(5);
        monthlyTournament.setStartTime(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)); // 1 mes

        Map<String, Object> monthlyRewards = new HashMap<>();
        monthlyRewards.put("first", Arrays.asList("NETHERITE_BLOCK:1", "ENCHANTED_GOLDEN_APPLE:16"));
        monthlyRewards.put("second", Arrays.asList("DIAMOND_BLOCK:3", "TOTEM_OF_UNDYING:1"));
        monthlyRewards.put("third", Arrays.asList("GOLD_BLOCK:5", "DIAMOND:16"));
        monthlyTournament.setRewards(monthlyRewards);

        tournaments.put("monthly", monthlyTournament);

        saveTournaments();
    }

    private void loadTournaments() {
        tournaments.clear();

        ConfigurationSection tournamentsSection = tournamentsConfig.getConfigurationSection("tournaments");
        if (tournamentsSection == null) return;

        for (String tournamentId : tournamentsSection.getKeys(false)) {
            try {
                Tournament tournament = new Tournament(tournamentId, "", Tournament.TournamentType.SINGLE_ELIMINATION);
                Map<String, Object> data = tournamentsSection.getConfigurationSection(tournamentId).getValues(true);
                tournament.deserialize(data);
                tournaments.put(tournamentId, tournament);

                // Si hay un torneo en progreso, activarlo
                if (tournament.getStatus() == Tournament.TournamentStatus.IN_PROGRESS) {
                    activeTournament = tournament;
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Error cargando torneo '" + tournamentId + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Cargados " + tournaments.size() + " torneos");
    }

    private void saveTournaments() {
        for (Map.Entry<String, Tournament> entry : tournaments.entrySet()) {
            tournamentsConfig.set("tournaments." + entry.getKey(), entry.getValue().serialize());
        }

        try {
            tournamentsConfig.save(tournamentsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error guardando torneos: " + e.getMessage());
        }
    }

    // ==================== GESTIÓN DE TORNEOS ====================

    public boolean createTournament(String id, String name, Tournament.TournamentType type) {
        if (tournaments.containsKey(id)) {
            return false;
        }

        Tournament tournament = new Tournament(id, name, type);
        tournaments.put(id, tournament);
        saveTournaments();

        return true;
    }

    public boolean startTournamentRegistration(String tournamentId) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null || tournament.getStatus() != Tournament.TournamentStatus.ANNOUNCED) {
            return false;
        }

        tournament.setStatus(Tournament.TournamentStatus.REGISTRATION_OPEN);
        saveTournaments();

        // Anunciar en el servidor
        announceTournamentRegistration(tournament);

        return true;
    }

    public boolean registerTeam(String tournamentId, Team team) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null || !tournament.canRegister()) {
            return false;
        }

        // Verificar tamaño del equipo
        if (team.getMemberCount() != tournament.getTeamSize()) {
            return false;
        }

        tournament.addParticipant(team);
        saveTournaments();

        // Notificar al equipo
        team.getOnlineMembers().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(ChatColor.GREEN + "¡Tu equipo se ha inscrito en el torneo '" +
                        tournament.getName() + "'!");
                member.sendMessage(ChatColor.GRAY + "El torneo comienza en: " +
                        tournament.getFormattedStartTime());
            }
        });

        // Verificar si se alcanzó el mínimo para comenzar
        if (tournament.getParticipantCount() >= tournament.getMinTeams()) {
            scheduleTournamentStart(tournament);
        }

        return true;
    }

    public boolean startTournament(String tournamentId) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null || tournament.getParticipantCount() < tournament.getMinTeams()) {
            return false;
        }

        tournament.setStatus(Tournament.TournamentStatus.IN_PROGRESS);
        activeTournament = tournament;
        tournament.setStartTime(System.currentTimeMillis());

        // Generar brackets según el tipo de torneo
        generateBrackets(tournament);

        saveTournaments();

        // Anunciar inicio
        announceTournamentStart(tournament);

        // Comenzar primera ronda
        startNextRound(tournament);

        return true;
    }

    // ==================== GENERACIÓN DE BRACKETS ====================

    private void generateBrackets(Tournament tournament) {
        List<Team> participants = tournament.getParticipants();
        Collections.shuffle(participants); // Aleatorizar orden

        switch (tournament.getType()) {
            case SINGLE_ELIMINATION:
                generateSingleEliminationBracket(tournament, participants);
                break;
            case DOUBLE_ELIMINATION:
                generateDoubleEliminationBracket(tournament, participants);
                break;
            case ROUND_ROBIN:
                generateRoundRobinBracket(tournament, participants);
                break;
            case SWISS:
                generateSwissBracket(tournament, participants);
                break;
        }
    }

    private void generateSingleEliminationBracket(Tournament tournament, List<Team> participants) {
        int roundNumber = 1;
        TournamentRound round = new TournamentRound(roundNumber);

        // Crear emparejamientos
        for (int i = 0; i < participants.size(); i += 2) {
            if (i + 1 < participants.size()) {
                TournamentMatch match = new TournamentMatch(participants.get(i), participants.get(i + 1));
                round.addMatch(match);
            } else {
                // Bye (avanza automáticamente)
                TournamentMatch match = new TournamentMatch(participants.get(i), null);
                match.setStatus(TournamentMatch.MatchStatus.TEAM1_WIN);
                match.setWinner(participants.get(i));
                round.addMatch(match);
            }
        }

        tournament.getRounds().put(roundNumber, round);
    }

    private void generateDoubleEliminationBracket(Tournament tournament, List<Team> participants) {
        // Implementación simplificada
        generateSingleEliminationBracket(tournament, participants);
        // Los perdedores van a un bracket de consolación
    }

    private void generateRoundRobinBracket(Tournament tournament, List<Team> participants) {
        int roundNumber = 1;

        // Cada equipo juega contra todos los demás
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                TournamentRound round = tournament.getRounds().getOrDefault(roundNumber, new TournamentRound(roundNumber));
                TournamentMatch match = new TournamentMatch(participants.get(i), participants.get(j));
                round.addMatch(match);
                tournament.getRounds().put(roundNumber, round);

                roundNumber++;
            }
        }
    }

    private void generateSwissBracket(Tournament tournament, List<Team> participants) {
        // Sistema suizo - equipos con records similares se enfrentan
        int roundNumber = 1;
        TournamentRound round = new TournamentRound(roundNumber);

        // Primera ronda aleatoria
        Collections.shuffle(participants);
        for (int i = 0; i < participants.size(); i += 2) {
            if (i + 1 < participants.size()) {
                TournamentMatch match = new TournamentMatch(participants.get(i), participants.get(i + 1));
                round.addMatch(match);
            }
        }

        tournament.getRounds().put(roundNumber, round);
    }

    // ==================== EJECUCIÓN DE TORNEOS ====================

    private void startNextRound(Tournament tournament) {
        int currentRound = tournament.getRounds().keySet().stream()
                .max(Integer::compare)
                .orElse(0);

        TournamentRound round = tournament.getRounds().get(currentRound);
        if (round == null || round.isCompleted()) {
            // Buscar siguiente ronda no completada
            for (int i = 1; i <= tournament.getRounds().size(); i++) {
                round = tournament.getRounds().get(i);
                if (round != null && !round.isCompleted()) {
                    currentRound = i;
                    break;
                }
            }
        }

        if (round == null || round.isCompleted()) {
            // Torneo completado
            finishTournament(tournament);
            return;
        }

        // Anunciar ronda
        announceRoundStart(tournament, round);

        // Programar partidos
        for (TournamentMatch match : round.getMatches()) {
            if (match.getStatus() == TournamentMatch.MatchStatus.SCHEDULED) {
                scheduleMatch(match, tournament, round);
            }
        }
    }

    private void scheduleMatch(TournamentMatch match, Tournament tournament, TournamentRound round) {
        match.setStatus(TournamentMatch.MatchStatus.IN_PROGRESS);
        match.setStartTime(System.currentTimeMillis());

        // Notificar a los equipos
        notifyMatchStart(match);

        // Programar fin del partido (30 minutos)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (match.getStatus() == TournamentMatch.MatchStatus.IN_PROGRESS) {
                // Tiempo agotado - decidir ganador
                resolveMatchTimeout(match);
                checkRoundCompletion(round, tournament);
            }
        }, 30 * 60 * 20L); // 30 minutos
    }

    public void reportMatchResult(String tournamentId, int roundNumber, int matchIndex, Team winner) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null) return;

        TournamentRound round = tournament.getRounds().get(roundNumber);
        if (round == null || matchIndex >= round.getMatches().size()) return;

        TournamentMatch match = round.getMatches().get(matchIndex);
        match.setWinner(winner);
        match.setStatus(winner.equals(match.getTeam1()) ?
                TournamentMatch.MatchStatus.TEAM1_WIN : TournamentMatch.MatchStatus.TEAM2_WIN);
        match.setEndTime(System.currentTimeMillis());

        // Anunciar resultado
        announceMatchResult(match);

        // Verificar si la ronda está completada
        checkRoundCompletion(round, tournament);
    }

    private void checkRoundCompletion(TournamentRound round, Tournament tournament) {
        boolean allCompleted = round.getMatches().stream()
                .allMatch(match -> match.getStatus() != TournamentMatch.MatchStatus.IN_PROGRESS &&
                        match.getStatus() != TournamentMatch.MatchStatus.SCHEDULED);

        if (allCompleted) {
            round.setCompleted(true);

            // Generar siguiente ronda si es necesario
            if (tournament.getType() == Tournament.TournamentType.SINGLE_ELIMINATION) {
                generateNextEliminationRound(tournament, round.getRoundNumber());
            }

            // Iniciar siguiente ronda
            startNextRound(tournament);
        }
    }

    private void generateNextEliminationRound(Tournament tournament, int currentRoundNumber) {
        TournamentRound currentRound = tournament.getRounds().get(currentRoundNumber);
        if (currentRound == null) return;

        List<Team> winners = currentRound.getMatches().stream()
                .filter(match -> match.getWinner() != null)
                .map(TournamentMatch::getWinner)
                .collect(Collectors.toList());

        if (winners.size() <= 1) {
            // Solo queda un ganador - final
            tournament.setWinner(winners.get(0));
            return;
        }

        // Crear siguiente ronda
        int nextRoundNumber = currentRoundNumber + 1;
        TournamentRound nextRound = new TournamentRound(nextRoundNumber);

        for (int i = 0; i < winners.size(); i += 2) {
            if (i + 1 < winners.size()) {
                TournamentMatch match = new TournamentMatch(winners.get(i), winners.get(i + 1));
                nextRound.addMatch(match);
            } else {
                // Bye
                TournamentMatch match = new TournamentMatch(winners.get(i), null);
                match.setStatus(TournamentMatch.MatchStatus.TEAM1_WIN);
                match.setWinner(winners.get(i));
                nextRound.addMatch(match);
            }
        }

        tournament.getRounds().put(nextRoundNumber, nextRound);
    }

    private void finishTournament(Tournament tournament) {
        tournament.setStatus(Tournament.TournamentStatus.FINISHED);
        tournament.setEndTime(System.currentTimeMillis());
        activeTournament = null;

        // Anunciar ganador
        announceTournamentWinner(tournament);

        // Dar recompensas
        giveTournamentRewards(tournament);

        saveTournaments();
    }

    // ==================== ANUNCIOS Y NOTIFICACIONES ====================

    private void announceTournamentRegistration(Tournament tournament) {
        String message = "§6§l¡INSCRIPCIONES ABIERTAS!";
        message += "\n§eTorneo: §6" + tournament.getName();
        message += "\n§eTipo: §f" + tournament.getType().getDisplayName();
        message += "\n§eEquipos: §f" + tournament.getTeamSize() + " jugadores";
        message += "\n§eInicio: §f" + tournament.getFormattedStartTime();
        message += "\n§eInscripción: §a/team tournament join " + tournament.getId();

        Bukkit.broadcastMessage("§6════════════════════════════");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("§6════════════════════════════");
    }

    private void announceTournamentStart(Tournament tournament) {
        String message = "§6§l¡TORNEO INICIADO!";
        message += "\n§e" + tournament.getName();
        message += "\n§eParticipantes: §f" + tournament.getParticipantCount() + " equipos";
        message += "\n§eSistema: §f" + tournament.getType().getDisplayName();
        message += "\n§e§l¡QUE COMIENCE LA BATALLA!";

        Bukkit.broadcastMessage("§6════════════════════════════");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("§6════════════════════════════");

        // Sonido especial
        org.tobi_hack.teamwars.utils.SoundManager.playSoundToAll("epic_moment");
    }

    private void announceRoundStart(Tournament tournament, TournamentRound round) {
        Bukkit.broadcastMessage("§6§lRONDA " + round.getRoundNumber() + " INICIADA");
        Bukkit.broadcastMessage("§ePartidos programados: §f" + round.getMatches().size());
    }

    private void notifyMatchStart(TournamentMatch match) {
        if (match.getTeam1() != null) {
            match.getTeam1().getOnlineMembers().forEach(memberId -> {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§6§l¡PARTIDO DE TORNEO!");
                    member.sendMessage("§eRival: §6" +
                            (match.getTeam2() != null ? match.getTeam2().getName() : "BYE"));
                    member.sendMessage("§ePrepárate para la batalla!");
                    member.playSound(member.getLocation(),
                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            });
        }

        if (match.getTeam2() != null) {
            match.getTeam2().getOnlineMembers().forEach(memberId -> {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§6§l¡PARTIDO DE TORNEO!");
                    member.sendMessage("§eRival: §6" + match.getTeam1().getName());
                    member.sendMessage("§ePrepárate para la batalla!");
                    member.playSound(member.getLocation(),
                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            });
        }
    }

    private void announceMatchResult(TournamentMatch match) {
        if (match.getWinner() == null) return;

        String message = "§6§lRESULTADO DE PARTIDO";
        message += "\n§e" + match.getTeam1().getName() + " §fvs §e" + match.getTeam2().getName();
        message += "\n§6Ganador: §e" + match.getWinner().getName();

        Bukkit.broadcastMessage(message);
    }

    private void announceTournamentWinner(Tournament tournament) {
        if (tournament.getWinner() == null) return;

        String message = "§6§l¡TORNEO FINALIZADO!";
        message += "\n§eTorneo: §6" + tournament.getName();
        message += "\n§6§lGANADOR: §e" + tournament.getWinner().getName();
        message += "\n§eFelicidades al equipo campeón!";

        Bukkit.broadcastMessage("§6════════════════════════════");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("§6════════════════════════════");

        // Efectos especiales para el ganador
        tournament.getWinner().getOnlineMembers().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendTitle("§6§l¡CAMPEONES!", "§eHas ganado el torneo", 10, 100, 20);
                org.tobi_hack.teamwars.utils.ParticleUtils.playVictoryCelebration(
                        member, tournament.getWinner().getColor());
            }
        });
    }

    private void giveTournamentRewards(Tournament tournament) {
        if (tournament.getWinner() == null) return;

        Map<String, Object> rewards = tournament.getRewards();

        // TODO: Implementar sistema de entrega de recompensas
        tournament.getWinner().getOnlineMembers().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage("§a¡Has recibido las recompensas del torneo!");
                // Dar items/XP/dinero aquí
            }
        });
    }

    private void resolveMatchTimeout(TournamentMatch match) {
        if (match.getTeam1() == null || match.getTeam2() == null) {
            match.setStatus(TournamentMatch.MatchStatus.CANCELLED);
            return;
        }

        // Decidir ganador por kills (simulado)
        int team1Kills = match.getTeam1().getKills();
        int team2Kills = match.getTeam2().getKills();

        if (team1Kills > team2Kills) {
            match.setWinner(match.getTeam1());
            match.setStatus(TournamentMatch.MatchStatus.TEAM1_WIN);
        } else if (team2Kills > team1Kills) {
            match.setWinner(match.getTeam2());
            match.setStatus(TournamentMatch.MatchStatus.TEAM2_WIN);
        } else {
            match.setStatus(TournamentMatch.MatchStatus.DRAW);
            // En caso de empate, gana el equipo con menos muertes
            if (match.getTeam1().getDeaths() < match.getTeam2().getDeaths()) {
                match.setWinner(match.getTeam1());
            } else {
                match.setWinner(match.getTeam2());
            }
        }

        match.setEndTime(System.currentTimeMillis());
    }

    private void scheduleTournamentStart(Tournament tournament) {
        long timeUntilStart = tournament.getStartTime() - System.currentTimeMillis();
        if (timeUntilStart <= 0) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startTournament(tournament.getId());
        }, timeUntilStart / 50); // Convertir a ticks
    }

    // ==================== GETTERS Y UTILIDADES ====================

    public Tournament getActiveTournament() {
        return activeTournament;
    }

    public List<Tournament> getOpenTournaments() {
        return tournaments.values().stream()
                .filter(t -> t.getStatus() == Tournament.TournamentStatus.REGISTRATION_OPEN)
                .collect(Collectors.toList());
    }

    public Tournament getTournament(String id) {
        return tournaments.get(id);
    }

    public List<Tournament> getAllTournaments() {
        return new ArrayList<>(tournaments.values());
    }

    public void reload() {
        loadTournaments();
    }
}