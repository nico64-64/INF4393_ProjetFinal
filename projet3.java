import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;

class Main
{
	public static void main (String args[])
	{
		ArbreSyntaxique arbre;
		boolean modeInteractif = false;
		
		// Gestion des arguments:
		if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h"))
		{
			System.out.println("\nprojet3.java");
			System.out.println("Résolveur d'expression arithmétiques utilisant un arbre syntaxique.\n");
			System.out.println("Usage: java projet3.java [--help (-h) | --interactif (-i)] \"expression\"\n");
			System.out.println("L'expression mathématique à résoudre doit être une expression arithmétique\n  entièrement parenthésée.");
			System.out.println("Une expression arithmétique entièrement parenthésée est une expression\n  mathématique où absolument chaque opération est entourée d'une parenthèse.");
			System.out.println("Il est fortement recommandé d'encadrer l'expression avec des guillemets\n  pour ne pas que votre shell effectue le calcul à la place du programme.");
			System.out.println("Exemple d'appel: java projet3.java \"(((3+1)/5)+(1%6))\"\n");
			System.out.println("Les opérateurs supportés sont l'addition (+), la soustraction (-),\n  la multiplication (*), la division entière (/), le modulo (%) et\n l'exposant (^).\n");
			System.out.println("Ce programme supporte les entiers décimaux positifs et des variables\n  (strings contenant exclusivement des lettres latines sans accents)\n  comme opérandes.\n");
			System.out.println("L'option --interactif (-i) permet d'effectuer plusieurs calculs\n  avec la même expression, mais en changeant la valeurs des variables.");
			return;
		}
		else if (args.length == 2 && (args[0].equals("--interactif") || args[0].equals("-i")))
		{
			modeInteractif = true;
			args[0] = args[1];
			System.out.println("Mode Interactif - Appuyez sur Ctrl-C pour quitter.\n");
		}
		else if (args.length != 1)
		{
			System.out.println("Veuillez spécifier une expression arithmétique entièrement parenthésée\n  et sans espace, ou passez l'argument --help pour en apprendre plus.");
			return;
		}
		
		// Parsing de l'expression et création de l'arbre:
		try
		{arbre = new ArbreSyntaxique(args[0]);}
		catch (IllegalArgumentException e)
		{
			System.out.println("Erreur: Votre expression arithmétique est invalide!");
			System.out.println(e.getMessage());
			return;
		}
		
		// Affichage du résultat (réécriture, résultat mathématique et dessin de l'arbre')
		System.out.print("\nExpression soumise: ");
		arbre.afficherExpression();
		System.out.println("Arbre de hauteur " + arbre.hauteur() + "\n");
		arbre.dessiner();
		
		// Modification des variables si en mode interactif:
		if (!modeInteractif)
		{return;}
		while (true)
		{
			arbre.definirVariables();
			System.out.print("\nExpression soumise: ");
			arbre.afficherExpression();
		}
	}
}


class Variable
// Représente une variable dans l'arbre syntaxique ou l'expression mathématique.
// Chaque variable a obligatoirement un nom (la string l'identifiant dans l'expression arithmétique) et facultativement une valeur associée (nombre entier).
// Toutefois, il sera impossible de calculer le résultat d'une expression contenant une variable sans valeur associée.
// On peut définir une variable (lui associer une valeur) à sa création ou avec variable.setValeur(entier).
// La fonction variable.setValeur() permet quant à elle de la définir interactivement via la console.
// On peut utiliser variable.estDefinie() pour savoir si une valeur est associée à cette variable, ainsi que variable.getValeur() pour obtenir sa valeur.
// On peut afficher son nom simplement avec System.out.println(variable).
{
	private String nom;
	private Integer valeur; // utilise la classe Integer afin de pouvoir stocker null si aucune valeur n'y est associée (variable non-définie)
	
	public Variable (String nom)
	{this(nom, null);}
	
	public Variable (String nom, Integer valeur)
	{
		this.nom = nom;
		this.valeur = valeur;
	}
	
	public void setValeur (int valeur)
	{this.valeur = valeur;}
	
	public void setValeur (Scanner scanner)
	// scanner doit pointer vers l'input utilisateur, donc vers stdin (System.in), normalement.
	{
		try
		{
			System.out.print("Veuillez entrer un nombre pour l'assigner à la variable \"" + nom + "\": ");
			valeur = scanner.nextInt();
			scanner.nextLine();
		}
		catch (InputMismatchException e)
		{
			System.out.println("Erreur: Vous devez entrer un nombre entier pour l'assigner à cette varaible.");
			scanner.nextLine();
			this.setValeur(scanner);
		}
	}
	
	public boolean estDefinie ()
	// Renvoie true si la variable est définie ou false si elle ne l'est pas.
	// Il est fortement recommandé d'utiliser cette fonction avant getValeur().
	{
		if (valeur == null)
		{return false;}
		return true;
	}
	
	@Override
	public String toString ()
	{return nom;}
	
	public int getValeur ()
	// Renvoie la valeur numérique associée à la variable ou throw un exception si elle n'en a pas.
	{
		if (valeur == null)
		{throw new IllegalStateException("Cette variable n'est pas définie!");}
		return valeur;
	}
}


class Element
// Contient un nombre ou un opérateur.
// Créer un élément avec le bon constructeur (seulement s'assurer de lui passer un int pour un nombre, un char pour un opérateur ou une Variable pour une variable) et le reste se fait automatiquement.
// Utiliser ensuite element.getType() pour savoir si c'est un opérateur (renvoie 'o'), une variable ('v') ou un nombre (renvoie 'n').
// Utiliser element.getElement() pour obtenir le nombre / la valeur de la variable (int) ou l'opérateur (char casté en int).
// Si on veut seulement afficher l'élément, on peut tout simplement utiliser System.out.println(element).
{
	private char type; //'n' pour nombre, 'v' pour variable ou 'o' pour operateur
	private int nombre;
	private char operateur;
	private Variable variable;
	
	
	public Element (int nombre)
	{
		this.type = 'n';
		this.nombre = nombre;
	}
	
	public Element (char operateur)
	{
		this.type = 'o';
		this.operateur = operateur;
	}
	
	public Element (Variable variable)
	{
		this.type = 'v';
		this.variable = variable;
	}
	
	@Override
	public String toString ()
	{
		if (type == 'o')
		{return operateur + "";}
		else if (type == 'n')
		{return nombre + "";}
		else if (type == 'v')
		{return variable.toString();}
		else
		{return "Erreur: État de l'élément invalide!";}
	}
	
	public char getType ()
	{return type;}
	
	public int getElement ()
	{
		if (type == 'n')
		{return nombre;}
		else if (type == 'o')
		{return (int) operateur;}
		else if (type == 'v')
		{
			if (variable.estDefinie())
			{return variable.getValeur();}
			throw new IllegalStateException("Cet élément est une variable qui n'a pas été définie!");
		}
		throw new IllegalStateException("Cet élément n'est ni un nombre ni un opérateur!");
	}
}


class Noeud
// Élément de base de l'arbre syntaxique.
// Contient un obligatoirement élément (nombre, variable ou opérateur) et facultativement jusqu'à 2 enfants.
// On doit généralement prendre le noeud racine pour créer un ArbreSyntaxique afin d'utiliser les Noeuds.
// L'exception est la méthode calculer(), qui calcule et renvoie le résultat que ce Noeud représente.
{
	private Element element;
	private Noeud enfantGauche;
	private Noeud enfantDroit;
	
	
	public Noeud (Element element)
	{this(element, null, null);}
	
	public Noeud (Element element, Noeud enfantGauche, Noeud enfantDroit)
	{
		this.element = element;
		this.enfantGauche = enfantGauche;
		this.enfantDroit = enfantDroit;
	}
	
	public Element getElement ()
	{return element;}
	
	public Noeud getEnfantGauche ()
	{return enfantGauche;}
	
	public Noeud getEnfantDroit ()
	{return enfantDroit;}
	
	public int calculer ()
	// Calcule (récursivement) le résultat que représente ce noeud.
	{
		if (element.getType() == 'n' || element.getType() == 'v')
		{return element.getElement();}
		else if (element.getType() == 'o' && enfantGauche != null && enfantDroit != null)
		{
			switch (element.getElement())
			{
				case '+':
					return enfantGauche.calculer() + enfantDroit.calculer();
				
				case '-':
					return enfantGauche.calculer() - enfantDroit.calculer();
				
				case '*':
					return enfantGauche.calculer() * enfantDroit.calculer();
				
				case '/':
					return enfantGauche.calculer() / enfantDroit.calculer();
				
				case '%':
					return enfantGauche.calculer() % enfantDroit.calculer();
				
				case '^':
					int valeur = enfantGauche.calculer(); // je créé des variables locales pour éviter de parcourir le sous-arbre à chaque itération
					int base = valeur;
					int max = enfantDroit.calculer();
					for (int i = 0; i < max; i++)
					{valeur *= base;}
					return valeur;
				
				default:
					throw new IllegalStateException("L'opérateur \"" + (char) element.getElement() + "\" est invalide!"); //ne devrait normalement pas jamais arriver
			}
		}
		throw new IllegalStateException("Ce noeud est dans un état invalide!"); // n'arrivera jamais, mais il faut faire taire le compilateur...
	}
}


class ArbreSyntaxique
// Arbre Syntaxique composé de Noeuds (la racine et ses descendants).
// On peut en construire un vide, avec un Noeud racine (potentiellement un sous-arbre) ou avec une expression arithmétique entièrement parenthésée (sous forme de String).
// Si des Variables sont utilisées, n'oubliez pas de les enregistrer avec arbre.ajouterVariable(), puis de les définir (si ce n'est pas déjà fait). On peut utiliser arbre.definirVariables pour tous les faire interactivement. Le constructeur utilisant une String fait tout ça automatiquement.
// Une fois l'arbre construit, on peut utiliser arbre.hauteur(), arbre.getResultat(), arbre.definirVariables(), arbre.afficherExpression() et arbre.dessiner().
// Voir les fonctions respectives pour en savoir plus.
{
	private Noeud racine;
	private ArrayList<Variable> listeVariables = new ArrayList<Variable>();
	
	
	public ArbreSyntaxique ()
	{this((Noeud) null);}
	
	public ArbreSyntaxique (Noeud racine)
	{this.racine = racine;}
	
	public ArbreSyntaxique (String expression)
	{
		int position[] = {0}; // pointeur (il faut faire ça pour le faire avaler à Java)
		racine = convertisseur(expression, position);
		definirVariables();
	}
	
	public void definirVariables ()
	// Demande à l'utilisateur de définir chaque variable.
	{
		Scanner scanner = new Scanner(System.in);
		listeVariables.forEach(variable -> variable.setValeur(scanner));
		//scanner.close();
	}
	
	public int hauteur ()
	// Renvoie la hauteur de l'arbre.
	{return hauteurNoeud(racine);}
	
	public int getResultat ()
	// Renvoie le résulat final de l'expression mathématique.
	{return racine.calculer();}
	
	public void afficherExpression ()
	// Affiche l'expression mathématque et son résultat dans la console.
	{expression(racine); System.out.println(" = " + racine.calculer());}
	
	public void dessiner ()
	// Dessine approximativement l'arbre dans la console.
	// La racine est à droite et l'arbre se développe de droite à gauche, où sont les feuilles.
	{dessinerSousArbre(racine, hauteurNoeud(racine));}
	
	
	// Les fonctions suivantes sont réservées à l'usage interne:
	
	private Noeud convertisseur (String expression, int position[])
	// Convertit une expression mathématique sous forme de String en un arbre syntaxique.
	// La String doit absolument être une "expression arithmétique entièrement parenthésée" et sans espace, sinon le résultat est indéfini!
	// La position est celle où le parseur est rendu. Créez un array d'un seul int contenant la valeur 0 pour le 1er appel. Java est stupide.
	// Cette fonction est appelée récursivement, ce qui explique qu'elle renvoie un Noeud (la racine de l'arbre) et non un ArbreSyntaxique.
	{
		if (position[0] >= expression.length()) // ne devrait normalement pas jamais arriver
		{throw new IllegalArgumentException("Vous devez positionner le curseur à l'intérieur de la string!");}
		
		if (expression.charAt(position[0]) >= '0' && expression.charAt(position[0]) <= '9')
		// nombre
		{
			int valeur = 0;
			do
			{
				valeur *= 10;
				valeur += expression.charAt(position[0]) - '0';
				position[0]++;
			} while (expression.charAt(position[0]) >= '0' && expression.charAt(position[0]) <= '9');
			return new Noeud(new Element(valeur));
		}
		
		else if (expression.charAt(position[0]) == '(')
		// expression (nombre/expression, opération et nombre/expression)
		{
			position[0]++;
			Noeud gauche = convertisseur(expression, position);
			char caractere = expression.charAt(position[0]);
			if (caractere != '+' && caractere != '-' && caractere != '*' && caractere != '/' && caractere != '%' && caractere != '^')
			{throw new IllegalArgumentException("Erreur: " + caractere + " n'est pas un opérateur reconnu.\nRéférez-vous à --help pour obtenir la liste des opérateurs acceptés par ce programme.");}
			Element operateur = new Element(caractere);
			position[0]++;
			Noeud droite = convertisseur(expression, position);
			position[0]++;
			return new Noeud(operateur, gauche, droite);
		}
		
		else if ((expression.charAt(position[0]) >= 'a' && expression.charAt(position[0]) <= 'z') || (expression.charAt(position[0]) >= 'A' && expression.charAt(position[0]) <= 'Z'))
		// variable
		{
			String nom = "";
			do
			{
				nom += expression.charAt(position[0]);
				position[0]++;
			} while ((expression.charAt(position[0]) >= 'a' && expression.charAt(position[0]) <= 'z') || (expression.charAt(position[0]) >= 'A' && expression.charAt(position[0]) <= 'Z'));
			listeVariables.add(new Variable(nom));
			return new Noeud(new Element(listeVariables.getLast()));
			
		}
		
		throw new IllegalArgumentException("Vous devez absolument fournir une expression arithmétique entièrement parenthésée sans espace!"); // erreur générique qui ne devrait normalement pas arriver
	}
	
	private static void expression (Noeud noeud)
	// Affiche l'expression mathématique du sous-arbre enraciné à noeud.
	// Fonction récursive interne.
	{
		if (noeud.getEnfantGauche() != null)
		{System.out.print('('); expression(noeud.getEnfantGauche());}
		System.out.print(noeud.getElement());
		if (noeud.getEnfantDroit() != null)
		{expression(noeud.getEnfantDroit()); System.out.print(')');}
	}
	
	private static int hauteurNoeud (Noeud noeud)
	// Trouve et renvoie la hauteur du noeud / sous-arbre enraciné en noeud.
	// Fonction récursive interne.
	{
		int hauteurGauche = -1, hauteurDroite = -1;
		
		if (noeud.getEnfantGauche() != null)
		{hauteurGauche = hauteurNoeud(noeud.getEnfantGauche());}
		
		if (noeud.getEnfantDroit() != null)
		{hauteurDroite = hauteurNoeud(noeud.getEnfantDroit());}
		
		if (hauteurGauche >= hauteurDroite)
		{return hauteurGauche + 1;}
		else
		{return hauteurDroite + 1;}
	}
	
	private static void dessinerSousArbre (Noeud noeud, int hauteur)
	// Dessine approximativement le sous-arbre enraciné en noeud dans la console.
	// Pour rendre l'algorithme plus efficace, il faut aussi lui fournir la heuteur du noeud racine pour éviter qu'on ait à la recalculer à chaque descendant.
	// Fonction récursive interne.
	{
		String decalage = "";
		String ramasseur = "> "; // le truc qui dit quel noeuds sont les enfants de quel noeud (c'est horrible, mais c'est le moins pire que j'ai trouvé)
		if (noeud.getEnfantGauche() == null || noeud.getEnfantDroit() == null)
		{ramasseur = "  ";}
		for (int i = 0; i < hauteur; i++)
		{decalage += "    ";}
		
		if (noeud.getEnfantGauche() != null)
		{dessinerSousArbre(noeud.getEnfantGauche(), hauteur - 1);}
		
		System.out.println(decalage + ramasseur + noeud.getElement());
		
		if (noeud.getEnfantDroit() != null)
		{dessinerSousArbre(noeud.getEnfantDroit(), hauteur - 1);}
	}
}
