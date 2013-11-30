package com.indianbau5;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;

public class Jisp {
    public static void main(String[] args) {
        StringBuilder src = new StringBuilder();
        for (Scanner s = new Scanner(args[0]); s.hasNext();)
            src.append(s.nextLine() + "\n");
        exec(parse(src.toString()));
    }
    public static void exec(LinkedList<Object> ast) {
        Env global = new Env(null) {{
            put("+", new Fn() {
                public Object apply(LinkedList<Object> args) {
                    return (Double)args.get(0) + (Double)args.get(1);
                }
            });
        }};
        for (Object o : ast)
            eval(o, global);
    }
    public static Object eval(Object o, Env env) {
        if (o instanceof Symbol)
            return env.get(o.toString());
        else if (o instanceof LinkedList) {
            LinkedList<Object> form = (LinkedList<Object>)o;
            String syn = form.get(0).toString();
            if (syn.equals("quote")) {
                return form.get(1);
            } else
            if (syn.equals("if")) {
                if ((Boolean)eval(form.get(1), env))
                    return eval(form.get(2), env);
                else return eval(form.get(3), env);
            } else
            if (syn.equals("set")) {
                String key = form.get(1).toString();
                return env.find(key).put(key, eval(form.get(2), env));
            } else
            if (syn.equals("def")) {
                return env.put(form.get(1).toString(), eval(form.get(2), env));
            } else
            if (syn.equals("fn")) {
                LinkedList<Object> tail = new LinkedList<Object>();
                for (int i = 1; i < form.size(); i++)
                    tail.add(form.get(i));
                return tail;
            } else
            if (syn.equals("begin")) {
                for (int i = 1; i < form.size() - 1; i++)
                    eval(form.get(i), env);
                return eval(form.getLast(), env);
            } else
            if (env.get(syn) instanceof Fn) {
                LinkedList<Object> args = new LinkedList<Object>();
                for (int i = 1; i < form.size(); i++)
                    args.add(eval(form.get(i), env));
                return ((Fn)env.get(syn)).apply(args);
            } else {
                LinkedList<Object> fn = (LinkedList<Object>)env.get(syn);
                LinkedList<Symbol> args = (LinkedList<Symbol>)fn.get(0);
                Object body = fn.get(1);
                Env inner = new Env(env);
                for (int i = 1; i < form.size(); i++)
                    inner.put(args.get(i - 1).toString(), eval(form.get(i), env));
                return eval(body, inner);
            }
        } else
            return o;
    }
    public static LinkedList<Object> parse(String src) {
        LinkedList<Object> tokens = new LinkedList<Object>();
        HashMap<Integer, Integer> inds = new HashMap<Integer, Integer>();
        Stack<Integer> s = new Stack<Integer>();
        for (int i = 0; i < src.length(); i++) {
            switch (src.charAt(i)) {
                case '(': s.push(i); break;
                case ')': inds.put(s.pop(), i); break;
            }
        }
        for (int i = 0; i < src.length(); i++) {
            StringBuilder token = new StringBuilder();
            if (Character.isWhitespace(src.charAt(i)))
                continue;
            switch (src.charAt(i)) {
                case '(':
                    tokens.add(parse(src.substring(i + 1, inds.get(i))));
                    i = inds.get(i); break;
                case '\'':
                    tokens.add(src.charAt(++i));
                    break;
                case '"':
                    for (; src.charAt(++i) != '"';
                         token.append(src.charAt(i)));
                    tokens.add(token.toString());
                    break;
                default:
                    for (; i < src.length() && !Character.isWhitespace(
                            src.charAt(i)); token.append(src.charAt(i++)));
                    try {
                        tokens.add(Double.parseDouble(token.toString()));
                    } catch (NumberFormatException nfe) {
                        if (token.toString().equals("true"))
                            tokens.add(true);
                        else if (token.toString().equals("false"))
                            tokens.add(false);
                        else tokens.add(new Symbol(token.toString()));
                    }
                    break;
            }
        }
        return tokens;
    }
}

class Symbol {
    public String sym;
    public Symbol(String sym) {
        this.sym = sym;
    }
    public String toString() {
        return sym;
    }
}

class Env extends HashMap<String, Object> {
    public Env outer;
    public Env(Env outer) {
        this.outer = outer;
    }
    public Env find(Object key) {
        return containsKey(key) ? this : outer.find(key);
    }
    public Object get(Object key) {
        return containsKey(key) ? super.get(key) : outer.get(key);
    }
}

interface Fn {
    public Object apply(LinkedList<Object> args);
}