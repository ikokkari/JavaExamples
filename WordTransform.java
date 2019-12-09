// An example of Strategy pattern, with different subalgorithms implemented
// as subclasses of the abstract subalgorithm for the main algorithm to use as
// part of its execution.

public class WordTransform {
  
    // Something that transforms a word into some other word.
    private static interface Transform {
        public String transform(String word);        
    }
    
    // Reverse the given word, maintaining case.
    private static class Reverse implements Transform {
        public String transform(String word) {
            StringBuilder result = new StringBuilder();
            boolean cap = Character.isUpperCase(word.charAt(0));
            for(int i = word.length() - 1; i >= 0; i--) {
                char c = word.charAt(i);
                if(i == word.length() - 1) {
                    result.append(cap ? Character.toUpperCase(c) : c);
                }
                else {
                    result.append(Character.toLowerCase(c));
                }
            }
            return result.toString();
        }
    }
    
    // Convert the word to pig latin, maintaining case.
    private static class PigLatin implements Transform {
        public String transform(String word) {
            boolean cap = Character.isUpperCase(word.charAt(0));
            int idx = 0;
            while(idx < word.length() && "AEIOUYaeiouy".indexOf(word.charAt(idx)) == -1) {
                idx++;
            }
            if(idx == 0) { 
                return word + "way";
            }
            else {
                String head;
                if(cap) {
                    head = Character.toUpperCase(word.charAt(idx)) + word.substring(idx + 1);
                }
                else {
                    head = word.substring(idx);
                }
                return head + word.substring(0, idx).toLowerCase() + "ay";
            }
        }
    }
    
    // Convert the word to ubbi dubbi, maintaining case.
    private static class UbbiDubbi implements Transform {
        public String transform(String word) {
            StringBuilder result = new StringBuilder();
            for(int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if("AEIOUYaeiouy".indexOf(c) == -1) {
                    result.append(c);
                }
                else {
                    if(Character.isUpperCase(c)) {
                        result.append("Ub" + Character.toLowerCase(c));
                    }
                    else {
                        result.append("ub" + c);
                    }
                }
            }
            return result.toString();
        }
    }

    // The main algorithm that transforms all the words in the given text, using
    // the Transform given to it as strategy object.
    public static String transformSentence(String phrase, Transform transformer) {
        int start = 0;
        boolean inWord = false;
        StringBuilder result = new StringBuilder();
        for(int i = 0; i <= phrase.length(); i++) {
            if(i == phrase.length() || !Character.isLetter(phrase.charAt(i))) {
                if(inWord) {
                    inWord = false;
                    String trans = transformer.transform(phrase.substring(start, i)); 
                    result.append( trans);
                }
                if(i < phrase.length()) { result.append(phrase.charAt(i)); }
            }
            else {
                if(!inWord) { start = i; }
                inWord = true;
            }
        }
        return result.toString();
    }
    
    public static void main(String[] args) {
        String s = "What does this become? We shall see!";
        System.out.println("Reverse:    " + transformSentence(s, new Reverse()));
        System.out.println("Pig latin:  " + transformSentence(s, new PigLatin()));
        System.out.println("Ubbi dubbi: " + transformSentence(s, new UbbiDubbi()));
    }
}