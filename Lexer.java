public class Lexer
{
    private static final char EOF        =  0;

    private Parser         yyparser; // parent parser object
    private java.io.Reader reader;   // input stream
    public int             lineno;   // line number
    public int             column;   // column

    int forward = 0;                // only ever updated in readCharFromBuffer() and reloadBuffer()
    int lexBegin = 0;               // should only be updated when switching states. 
    int accumulator = 1;            // always increments with column, with the exception that accumulator is never set to lexBegin. 

    char[] bufferOne = new char[10];
    char[] bufferTwo = new char[10];
    boolean currentBuffer = false; // false = second, true = first 
    boolean firstLoad = true;

    String[] keywords = {"int ", "print ", "var ", "func ", "if ", "else ", "while ", "void "};

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 1;
    }

    
    public char NextChar() throws Exception
    {
        int data = reader.read();
        if(data == -1)
        {
            return EOF;
        }
        return (char)data;
    }
    public int Fail()
    {
        return -1;
    }

    // * If yylex reach to the end of file, return  0
    // * If there is an lexical error found, return -1
    // * If a proper lexeme is determined, return token <token-id, token-attribute> as follows:
    //   1. set token-attribute into yyparser.yylval
    //   2. return token-id defined in Parser
    //   token attribute can be lexeme, line number, colume, etc.
    public int yylex() throws Exception
    {

        /*
            1. read character from stream.
            2. evaluate state transition based on character.
            3. keep going until a valid lexeme is found. 
        */

        /*
         * Set lexBegin = column on switchback to state = 0, lexBegin = accumulator on switch from state = 0.
         * Always increment accumulator after a character is read.
         */
        int state = 0;
        StringBuilder builder = new StringBuilder();

        if (firstLoad)
            reloadBuffer();
        firstLoad = false;

        while(true)
        {
            char c = readCharFromBuffer();

            column = accumulator;

            if (c == EOF && forward != 9) {state = 9999;}

            boolean isUppercaseLetter = (c >= 65 && c <= 90);
            boolean isLowercaseLetter = (c >= 97 && c <= 122);
            boolean isNumber = (c >= 48 && c <= 57);

            boolean isWhiteSpace = (c == 32 || c == 9 || c == 13); // 32 = space, 9 = \t, 13 = \r 
            boolean isNewLine = (c == 10);
            boolean isPeriod = (c == 46);
            
            switch(state)
            { 
                case 0 :    // neutral state between states. keep reading characters, if anything besides whitespace is encountered, jump states.
                    
                if (isLowercaseLetter || isUppercaseLetter) // first character is an alphabetical.
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        builder.append(c); 
                        state = 100;
                        continue;
                    }
                    else if (isNumber) // first character read is a number.
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        builder.append(c);
                        state = 1000;
                        continue;
                    }
                    else if (isWhiteSpace) // read character is whitespace. skip.
                    {
                        if (c == 32) // whitespace is just 1 space
                        {
                            accumulator++;
                            continue;
                        }
                        else if (c == 9) // whitespace is a tab, not just a space. unless you're na, where tab just equals 1 fucking space for some reason.
                        {
                            accumulator++;
                            //accumulator += 5;
                            continue;
                        }
                    }
                    else if (isNewLine)
                    {
                        lineno++;
                        accumulator = 1;
                        lexBegin = 1;
                        continue;
                    }
                    else if (c == '(')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)'(');
                        return Parser.LPAREN;   
                    }
                    else if (c == ')')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)')');
                        return Parser.RPAREN;   
                    }
                    else if (c == '{')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)'{');
                        return Parser.BEGIN;
                    }
                    else if (c == '}')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)'}');
                        return Parser.END;
                    }
                    else if (c == '_')
                    {
                        return -1;
                    }
                    else if (c == '<')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 200;
                        builder.append(c);
                        continue;
                    }
                    else if (c == '>')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 201;
                        builder.append(c);
                        continue;
                    }
                    else if (c == '=')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)'=');
                        return Parser.RELOP;
                    }
                    else if (c == '-')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 202;
                        builder.append(c);
                        continue;
                    }
                    else if (c == '+')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)"+");
                        return Parser.OP;
                    }
                    else if (c == '*')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)'*');
                        return Parser.OP;
                    }
                    else if (c == '/')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 203;
                        builder.append(c);
                        continue;
                    }
                    else if (c == ';')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        column = lexBegin;
                        yyparser.yylval = new ParserVal((Object)';');
                        return Parser.SEMI;
                    }
                    else if (c == ':')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 2; continue;
                    }
                    else if (c == '!')
                    {
                        lexBegin = accumulator;
                        accumulator++;
                        state = 3; continue;
                    }
                    else if (c == ',')
                    {
                        accumulator++;
                        yyparser.yylval = new ParserVal((Object)',');
                        return Parser.COMMA;
                    }
                    else if (c == '.') // comma cannot be the first character read from whitespace
                    {
                        return -1;
                    }
                    break; // <-- safety measure, just in case all if statements evaluate false for whatever reason. 
                    
                case 2 :    // character is a colon.
                    if (c == ':')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)"::");
                        return Parser.TYPEOF;
                    }
                case 3 :    // character is !
                {
                    if (c == '=')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)"!=");
                        return Parser.RELOP;
                    }
                    else // idk why ! would ever be by itself, and it doesn't seem to ever occur in the tests. it just returns an error instead.
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        return -1;
                    }
                }
                case 100 :  // case of a alphabetic character being the first read.
                   
                    // if another character or number, just append it to the current string, add to accumulator.
                    if (isLowercaseLetter || isUppercaseLetter || isNumber || c == '_') 
                    {
                        accumulator++;
                        builder.append(c);
                        continue;
                    }
                    // must be a keyword/id
                    else if (isWhiteSpace || isNewLine || c == '(' || c == ')' || c == ';' || c == '!' || 
                                 c == '+' || c == '-'  || c == '/' || c == '*' || c == '<' || c == '=' || c == '>') 
                    {
                        column = lexBegin;
                        lexBegin = accumulator;                        
                        state = 0;
                        builder.append(c);
                        retract();

                        state = 0;
                        switch (builder.substring(0,builder.length()-1))
                        {
                            case "int" :
                                yyparser.yylval = new ParserVal((Object)"int");
                                return Parser.INT;
                            case "print" :
                                yyparser.yylval = new ParserVal((Object)"print");
                                return Parser.PRINT;
                            case "var" :
                                yyparser.yylval = new ParserVal((Object)"var");
                                return Parser.VAR;
                            case "func" :
                                yyparser.yylval = new ParserVal((Object)"func");
                                return Parser.FUNC;
                            case "if" :
                                yyparser.yylval = new ParserVal((Object)"if");
                                return Parser.IF;
                            case "else" :
                                yyparser.yylval = new ParserVal((Object)"else");
                                return Parser.ELSE;
                            case "while" :
                                yyparser.yylval = new ParserVal((Object)"while");
                                return Parser.WHILE;
                            case "void" :
                                yyparser.yylval = new ParserVal((Object)"void");
                                return Parser.VOID;
                            
                            // the lexeme is an id, not a keyword.
                            default :
                                yyparser.yylval = new ParserVal((Object)builder.toString().substring(0,builder.length() - 1));
                                return Parser.ID;
                        }
                    }
                case 200 :  // character read before is a <
                {
                    if (c == '=')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)"<=");
                        return Parser.RELOP;
                    }
                    else if (c == '-')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)"<-");
                        return Parser.ASSIGN;
                    }
                    else
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        retract();
                        yyparser.yylval = new ParserVal((Object)"<"); 
                        return Parser.RELOP;
                    }
                }
                case 201 :  // character read before is a >
                {
                    if (c == '=')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)">=");
                        return Parser.RELOP;
                    }
                    else
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        retract();
                        yyparser.yylval = new ParserVal((Object)">"); 
                        return Parser.RELOP;
                    }
                }
                case 202 :  // character read before is a -
                {
                    if (c == '>')
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        yyparser.yylval = new ParserVal((Object)"->");
                        return Parser.FUNCRET;
                    }
                    else 
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        retract();
                        yyparser.yylval = new ParserVal((Object)"-");
                        return Parser.OP;
                    }
                }
                
                case 203  : // character read before is a /
                {
                    if (c == '/') // if it is a comment, ignore the rest of this line.
                    {
                        accumulator = 1;
                        lineno++;
                        lexBegin = 0;
                        state = 300;
                        continue;
                    }
                    else
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        retract();
                        yyparser.yylval = new ParserVal((Object)'/');
                        return Parser.OP;
                    }
                }
                case 300 :  // make sure to ignore comments.
                {
                    if (c == '\n')
                    {
                        state = 0;
                        continue;
                    }
                }
                case 1000 : // case of a number being the first read. 
                    
                    if (isPeriod)
                    {
                        accumulator++;
                        builder.append(c);
                        state = 1001;
                        continue;
                    }
                    else if (isNumber)
                    {
                        accumulator++;
                        builder.append(c);
                        continue;
                    }
                    else // anything at all has interrupted the token. retract 1 to account for the interrupt.
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        builder.append(c);
                        retract();
                        yyparser.yylval = new ParserVal((Object)builder.toString().substring(0,builder.length()-1));
                        return Parser.NUM;
                    }

                case 1001 : // case of a number then a period, make sure period doesn't happen again.
                {
                    if (isPeriod) // retract so the next character is a period, guarantee failure.
                    {
                        accumulator++;
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        builder.append(c);
                        retract();
                        yyparser.yylval = new ParserVal((Object)builder.toString().substring(0,builder.length()-1));
                        // make sure there isn't a valid number here, as it could still be registered as a token.
                        // i.e. 3.. <-- invalid | 123.456.789 <-- "valid"
                        if (builder.length() >= 2                               &&
                            builder.toString().charAt(builder.length()-2) >= 48 &&
                            builder.toString().charAt(builder.length()-2) <= 57   )
                            {
                                accumulator--;
                                return Parser.NUM;
                            }
                        return -1;
                    }
                    else if (isNumber)
                    {
                        accumulator++;
                        builder.append(c);
                        continue;
                    }
                    else // anything at all has ended the token. retract 1 to account for the interrupt.
                    {
                        column = lexBegin;
                        lexBegin = accumulator;
                        state = 0;
                        builder.append(c);
                        retract();
                        yyparser.yylval = new ParserVal((Object)builder.toString().substring(0,builder.length()-1));
                        return Parser.NUM;
                    }
                }
                case 9999 : // end of file
                    return EOF;
            }
        }
    }
    
    // populate both buffers with the next 20 characters.
    void reloadBuffer() throws Exception
    {
        if (!currentBuffer)  // first buffer currently active, populate bufferTwo
        {
            for (int i = 0; i < 9; i++)
            {
                bufferOne[i] = NextChar();     
            }
            bufferOne[9] = EOF;
        }
        else if (currentBuffer) // second buffer currently active, populate bufferOne
        {
            for (int i = 0; i < 9; i++)
            {
                bufferTwo[i] = NextChar();
            }
            bufferTwo[9] = EOF;
        }
        currentBuffer = !currentBuffer;
        forward = -1;       // go to before the start of the current buffer before reading.
    }

    void retract() {forward --;} // subtract 1 from forward.

    char readCharFromBuffer() throws Exception
    {
        forward++;
        char c = EOF;
        if (!currentBuffer)
        {
            if (bufferTwo[forward] == EOF && forward == 9)
            {
                reloadBuffer();
                forward++;
                c = bufferOne[forward]; // read from 0 at bufferTwo.
            }
            else 
                c = bufferTwo[forward];
        }
        else if (currentBuffer)
        {
            if (bufferOne[forward] == EOF && forward == 9)
            {
                reloadBuffer();
                forward++;
                c = bufferTwo[forward];
            }
            else
                c = bufferOne[forward];
        }
        else 
        {
            reloadBuffer();
            forward++;
            c = bufferOne[forward];
        }
        return c;
    }

}
