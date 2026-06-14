// Importation des bibliothèques nécessaires
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Programme permettant de calculer le gain ou la perte en capital
 * d'un portefeuille d'actions selon la méthode FIFO
 * (First In, First Out : premier acheté, premier vendu).
 *
 * Les transactions sont saisies par l'utilisateur sous la forme :
 * - buy x shares at $y each
 * - sell x shares at $y each
 *
 * Lors d'une vente, les actions vendues sont retirées en priorité
 * des lots les plus anciens du portefeuille.
 */
public class exo1projet {

    /**
     * Classe représentant un lot d'actions acheté.
     *
     * quantity : nombre d'actions dans le lot
     * price    : prix unitaire payé lors de l'achat
     */
    static class Lot {
        int quantity;
        int price;

        /**
         * Constructeur d'un lot.
         *
         * @param quantity nombre d'actions achetées
         * @param price prix unitaire de l'action
         */
        Lot(int quantity, int price) {
            this.quantity = quantity;
            this.price = price;
        }
    }

    /**
     * Expression régulière utilisée pour vérifier et analyser
     * le format des transactions.
     *
     * Exemple accepté :
     * buy 100 shares at $20 each
     * sell 50 shares at $30 each
     *
     * Groupes capturés :
     * 1 -> buy ou sell
     * 2 -> quantité
     * 3 -> prix unitaire
     */
    private static final Pattern PATTERN = Pattern.compile(
            "^(buy|sell)\\s+(\\d+)\\s+shares?\\s+at\\s+\\$(\\d+)\\s+each$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Calcule le gain total en capital selon le protocole FIFO.
     *
     * Principe :
     * - Les achats sont placés dans une file FIFO.
     * - Lors d'une vente, les actions sont retirées à partir
     *   des lots les plus anciens.
     * - Le gain est calculé comme :
     *      prix de vente - prix d'achat
     *
     * @param input ensemble des transactions séparées par des retours à la ligne
     * @return gain total en capital (peut être négatif)
     */
    public static int computeCapitalGain(String input) {

        // File FIFO contenant tous les lots achetés
        Deque<Lot> stock = new ArrayDeque<>();

        // Accumulateur du gain total
        int totalGain = 0;

        // Compteur servant à identifier la ligne courante
        int day = 0;

        // Lecture ligne par ligne des transactions
        for (String line : input.split("\\r?\\n")) {

            // Suppression des espaces inutiles
            line = line.trim();

            // Ignorer les lignes vides
            if (line.isEmpty()) {
                continue;
            }

            day++;

            // Vérification du format de la transaction
            Matcher m = PATTERN.matcher(line);

            if (!m.matches()) {
                throw new IllegalArgumentException(
                        "Ligne " + day + " mal formée : " + line
                );
            }

            // Récupération des informations extraites
            String action = m.group(1).toLowerCase();
            int qty = Integer.parseInt(m.group(2));
            int price = Integer.parseInt(m.group(3));

            /*
             * CAS 1 : ACHAT
             * On crée un nouveau lot et on l'ajoute à la fin
             * de la file FIFO.
             */
            if (action.equals("buy")) {

                stock.addLast(new Lot(qty, price));

            }
            /*
             * CAS 2 : VENTE
             * On retire les actions à partir des lots les plus anciens.
             */
            else {

                int remaining = qty;

                // Continuer jusqu'à ce que toutes les actions soient vendues
                while (remaining > 0) {

                    // Vérification que le portefeuille contient assez d'actions
                    if (stock.isEmpty()) {
                        throw new IllegalStateException(
                                "Portefeuille insuffisant."
                        );
                    }

                    // Consultation du lot le plus ancien
                    Lot lot = stock.peekFirst();

                    /*
                     * Si le lot contient moins ou exactement
                     * le nombre d'actions à vendre :
                     */
                    if (lot.quantity <= remaining) {

                        // Calcul du gain réalisé sur tout le lot
                        totalGain += lot.quantity * (price - lot.price);

                        // Mise à jour du nombre d'actions restant à vendre
                        remaining -= lot.quantity;

                        // Retrait du lot épuisé
                        stock.pollFirst();
                    }
                    /*
                     * Sinon, seule une partie du lot est vendue.
                     */
                    else {

                        // Gain sur les actions effectivement vendues
                        totalGain += remaining * (price - lot.price);

                        // Mise à jour de la quantité restante dans le lot
                        lot.quantity -= remaining;

                        // Toutes les actions demandées ont été vendues
                        remaining = 0;
                    }
                }
            }
        }

        // Retour du gain total calculé
        return totalGain;
    }

    /**
     * Point d'entrée du programme.
     *
     * Lecture des transactions saisies par l'utilisateur
     * jusqu'à ce qu'une ligne vide soit rencontrée.
     */
    public static void main(String[] args) {

        System.out.println(
                "Entrez les transactions (ligne vide pour terminer) :"
        );

        Scanner scanner = new Scanner(System.in);

        // Stockage temporaire des transactions saisies
        StringBuilder sb = new StringBuilder();

        // Lecture des lignes
        while (scanner.hasNextLine()) {

            String line = scanner.nextLine();

            // Fin de saisie si ligne vide
            if (line.trim().isEmpty()) {
                break;
            }

            sb.append(line).append("\n");
        }

        try {

            // Calcul du gain total
            int gain = computeCapitalGain(sb.toString());

            // Affichage du résultat
            if (gain >= 0) {
                System.out.println(
                        "\nGain en capital total : " + gain + " $"
                );
            } else {
                System.out.println(
                        "\nPerte en capital totale : "
                                + Math.abs(gain) + " $"
                );
            }

        } catch (IllegalArgumentException | IllegalStateException e) {

            // Gestion des erreurs de saisie ou de portefeuille
            System.out.println("Erreur : " + e.getMessage());
        }

        scanner.close();
    }
}
