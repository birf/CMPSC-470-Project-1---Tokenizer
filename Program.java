public class Program {
    public static void main(String[] args) throws Exception
    {
        //java.io.Reader r = new java.io.StringReader
        //("func main()->int\n"
        //+"{\n"
        //+"    var int a;\n"
        //+"    a <- bcd + 2 * 3;\n"
        //+"    b <- 1.234\n"
        //+"    print a;\n"
        //+"}\n"
        //);
        //

        //"test1.minc","test2.minc","test3.minc","test4.minc","test5.minc" , "test6.minc", "test7.minc",
        // , "test5.minc", "test6.minc", "test7.minc", "test8.minc"
         args = new String[] { "test1.minc","test2.minc","test3.minc","test4.minc","test5.minc" , "test6.minc", "test7.minc","test8.minc"};

        if(args.length <= 0)
            return;    
        for (int i = 0; i < args.length; i++)
        {
            java.io.Reader r = new java.io.FileReader(args[i]);
            Compiler compiler = new Compiler(r);
            compiler.Compile();
            System.out.println();
        }
    }
}
