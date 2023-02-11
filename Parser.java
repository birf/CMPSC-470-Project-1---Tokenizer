public class Parser
{
    public static final int OP         = 10;    // "+", "-", "*", "/"
    public static final int RELOP      = 11;    // "<", ">", "=", "!=", "<=", ">="
    public static final int TYPEOF     = 12;    // "::"
    public static final int ASSIGN     = 13;    // "<-"
    public static final int LPAREN     = 14;    // "("
    public static final int RPAREN     = 15;    // ")"
    public static final int SEMI       = 16;    // ";"
    public static final int COMMA      = 17;    // ","
    public static final int FUNCRET    = 18;    // "->"
    public static final int NUM        = 19;    // number
    public static final int ID         = 20;    // identifier
    public static final int BEGIN      = 21;    // "{"
    public static final int END        = 22;    // "}"
    public static final int INT        = 23;    // "int"
    public static final int PRINT      = 24;    // "print"
    public static final int VAR        = 25;    // "var"
    public static final int FUNC       = 26;    // "func"
    public static final int IF         = 27;    // "if"
    public static final int ELSE       = 28;    // "else"
    public static final int WHILE      = 29;    // "while"
    public static final int VOID       = 30;    // "void"

    // String[] keywords = {"int ", "print ", "var ", "func ", "if ", "else ", "while ", "void "};

    Compiler         compiler;
    Lexer            lexer;     // lexer.yylex() returns token-name
    public ParserVal yylval;    // yylval contains token-attribute

    String[] tokens = {"OP", "RELOP", "TYPEOF", "ASSIGN", "LPAREN","RPAREN","SEMI","COMMA","FUNCRET","NUM","ID","BEGIN","END","INT","PRINT",
                       "VAR","FUNC","IF","ELSE","WHILE","VOID"};

    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        this.compiler = compiler;
        this.lexer    = new Lexer(r, this);
    }

    public int yyparse() throws Exception
    {
        while ( true )
        {
            int token = lexer.yylex();  // get next token-name
            Object attr = yylval.obj;   // get      token-attribute
            
            if(token == 0)
            {
                // EOF is reached
                System.out.println("Success!");
                return 0;
            }
            if(token == -1)
            {
                // lexical error is found
                System.out.println("Error! There is a lexical error at " + lexer.lineno + ":" + lexer.column + ".");
                return -1;
            }

            String tokenname = tokens[token - 10];
            
            System.out.println("<" + tokenname + ", token-attr:\"" + attr + "\", " + lexer.lineno + ":" + lexer.column + ">");
        }
    }
}
