package org.fundacionjala.sonarqube.parser;

import com.sonar.sslr.api.typed.GrammarBuilder;
import com.sonar.sslr.api.typed.Optional;
import org.fundacionjala.sonarqube.InternalSyntaxToken;
import org.fundacionjala.sonarqube.api.ApexKeyword;
import org.fundacionjala.sonarqube.api.ApexPunctuator;
import org.fundacionjala.sonarqube.tree.*;
import org.fundacionjala.sonarqube.treeimplementation.*;

import java.util.List;

public class ApexGrammar {

    private final GrammarBuilder<InternalSyntaxToken> b;
    private final TreeFactory f;

    public ApexGrammar(GrammarBuilder<InternalSyntaxToken> b, TreeFactory f) {
        this.b = b;
        this.f = f;
    }

    public ModifiersTreeImpl MODIFIERS() {
        return b.<ModifiersTreeImpl>nonterminal(ApexLexer.MODIFIERS)
                .is(
                        f.modifiers(
                                b.<ModifierTree>oneOrMore(MODIFIER_KEYWORD())));
    }

    public ModifierKeywordTreeImpl MODIFIER_KEYWORD() {
        return b.<ModifierKeywordTreeImpl>nonterminal().is(
                f.modifierKeyword(
                        b.firstOf(
                                b.token(ApexKeyword.PUBLIC),
                                b.token(ApexKeyword.PROTECTED),
                                b.token(ApexKeyword.STATIC),
                                b.token(ApexKeyword.PRIVATE),
                                b.token(ApexKeyword.GLOBAL),
                                b.token(ApexKeyword.FINAL),
                                b.token(ApexKeyword.ABSTRACT),
                                b.token(ApexKeyword.TRANSIENT))));
    }

    //region Compilation Unit
    public CompilationUnitTreeImpl COMPILATION_UNIT() {
        return b.<CompilationUnitTreeImpl>nonterminal(ApexLexer.COMPILATION_UNIT)
                .is(
                        f.newCompilationUnit(
                                b.oneOrMore(TYPE_DECLARATION()),
                                b.token(ApexLexer.EOF)));
    }

    public Tree TYPE_DECLARATION() {
        return b.<Tree>nonterminal(ApexLexer.TYPE_DECLARATION)
                .is(
                        f.newTypeDeclaration(
                                MODIFIERS(),
                                CLASS_DECLARATION()
                        ));
    }

    public ClassTreeImpl CLASS_DECLARATION() {
        return b.<ClassTreeImpl>nonterminal(ApexLexer.CLASS_DECLARATION)
                .is(
                        f.newClassDeclaration(
                                b.optional(
                                        f.getSharingRules(
                                                b.firstOf(
                                                        b.token(ApexKeyword.WITHOUT),
                                                        b.token(ApexKeyword.WITH)),
                                                b.token(ApexKeyword.SHARING))),
                                b.token(ApexKeyword.CLASS),
                                NAMING_KEYWORDS(),
                                b.optional(EXTENDS_LIST()),
                                CLASS_BODY()));
    }

    public List<InternalSyntaxToken> EXTENDS_LIST() {
        return b.<List<InternalSyntaxToken>>nonterminal(ApexLexer.EXTENDS_DECLARATION)
                .is(
                        f.defineExtendsDeclaration(
                                b.token(ApexKeyword.EXTENDS),
                                b.token(ApexLexer.IDENTIFIER)
                        )
                );
    }

    public NamingKeywordsTreeImpl NAMING_KEYWORDS() {
        return b.<NamingKeywordsTreeImpl>nonterminal().is(
                f.namingKeywords(
                        b.firstOf(
                                ALLOWED_KEYWORDS_AS_IDENTIFIER(),
                                SPECIAL_KEYWORDS_AS_IDENTIFIER()
                        )
                )
        );
    }

    public InternalSyntaxToken ALLOWED_KEYWORDS_AS_IDENTIFIER() {
        return b.<InternalSyntaxToken>nonterminal(ApexLexer.ALLOWED_KEYWORDS_AS_IDENTIFIER).is(
                b.firstOf(
                        b.token(ApexLexer.IDENTIFIER),
                        b.token(ApexKeyword.TRANSIENT),
                        b.token(ApexKeyword.RETURNING),
                        b.token(ApexKeyword.SEARCH),
                        b.token(ApexKeyword.STAT),
                        b.token(ApexKeyword.CONVERTCURRENCY),
                        b.token(ApexKeyword.SAVEPOINT),
                        b.token(ApexKeyword.TOLABEL),
                        b.token(ApexKeyword.SHARING),
                        b.token(ApexKeyword.GET),
                        b.token(ApexKeyword.AFTER),
                        b.token(ApexKeyword.BEFORE),
                        b.token(ApexKeyword.FIRST),
                        b.token(ApexKeyword.LAST),
                        b.token(ApexKeyword.CATEGORY),
                        b.token(ApexKeyword.NETWORK),
                        b.token(ApexKeyword.ITERATOR)
                ));
    }

    public InternalSyntaxToken SPECIAL_KEYWORDS_AS_IDENTIFIER() {
        return b.<InternalSyntaxToken>nonterminal(ApexLexer.SPECIAL_KEYWORDS_AS_IDENTIFIER).is(
                b.firstOf(
                        b.token(ApexKeyword.WITHOUT),
                        b.token(ApexKeyword.OFFSET),
                        b.token(ApexKeyword.DATA),
                        b.token(ApexKeyword.GROUP),
                        b.token(ApexKeyword.LIMIT)
                ));
    }
    //endregion Compilation Unit

    public ClassTreeImpl CLASS_BODY() {
        return b.<ClassTreeImpl>nonterminal(ApexLexer.CLASS_BODY)
                .is(
                        f.newClassBody(
                                b.token(ApexPunctuator.LBRACE),
                                b.oneOrMore(CLASS_OR_INTERFACE_MEMBER()),
                                b.token(ApexPunctuator.RBRACE)));
    }

    public ApexTree CLASS_OR_INTERFACE_MEMBER() {
        return b.<ApexTree>nonterminal(ApexLexer.MEMBER_DECLARATION)
                .is(
                        b.firstOf(
                                f.completeMember(
                                        MODIFIERS(),
                                        METHOD_DECLARATION()
                                ),
                                f.newEmptyMember(b.token(ApexPunctuator.SEMICOLON))));
    }

    public MethodTreeImpl METHOD_DECLARATION() {
        return b.<MethodTreeImpl>nonterminal()
                .is(
                        f.newMethod(
                                TYPE(),
                                b.token(ApexLexer.IDENTIFIER),
                                FORMAL_PARAMETERS(),
                                BLOCK()));
    }

    public BlockTreeImpl BLOCK() {
        return b.<BlockTreeImpl>nonterminal(ApexLexer.BLOCK)
                .is(f.block(b.token(ApexPunctuator.LBRACE), BLOCK_STATEMENTS(), b.token(ApexPunctuator.RBRACE)));
    }

    public BlockStatementListTreeImpl BLOCK_STATEMENTS() {
        return b.<BlockStatementListTreeImpl>nonterminal(ApexLexer.BLOCK_STATEMENTS)
                .is(f.blockStatements(b.zeroOrMore(BLOCK_STATEMENT())));
    }

    public BlockStatementListTreeImpl BLOCK_STATEMENT() {
        return b.<BlockStatementListTreeImpl>nonterminal(ApexLexer.BLOCK_STATEMENT)
                .is(
                        f.wrapInBlockStatements(
                                b.firstOf(
                                        STATEMENT(),
                                        LOCAL_VARIABLE_DECLARATION()
                                )
                        )
                );
    }

    public VariableTreeImpl LOCAL_VARIABLE_DECLARATION() {
        return b.<VariableTreeImpl>nonterminal(ApexLexer.LOCAL_VARIABLE_DECLARATION)
                .is(VARIABLE_DECLARATOR_ID());
    }

    public StatementTree STATEMENT() {
        return b.<StatementTree>nonterminal(ApexLexer.STATEMENT)
                .is(
                        b.firstOf(
                                BLOCK(),
                                IF_STATEMENT(),
                                EMPTY_STATEMENT(),
                                EXPRESSION_STATEMENT()));
    }

    //region Expression
    public ExpressionStatementTreeImpl EXPRESSION_STATEMENT() {
        return b.<ExpressionStatementTreeImpl>nonterminal(ApexLexer.EXPRESSION_STATEMENT)
                .is(f.expressionStatement(EXPRESSION(), b.token(ApexPunctuator.SEMICOLON)));
    }

    public IfStatementTreeImpl IF_STATEMENT() {
        return b.<IfStatementTreeImpl>nonterminal(ApexLexer.IF_STATEMENT)
                .is(
                        f.completeIf(
                                b.token(ApexKeyword.IF),
                                b.token(ApexPunctuator.LPAREN),
                                EXPRESSION(),
                                b.token(ApexPunctuator.RPAREN),
                                STATEMENT(),
                                b.optional(
                                        f.newIfWithElse(b.token(ApexKeyword.ELSE), STATEMENT()))));
    }

    public EmptyStatementTreeImpl EMPTY_STATEMENT() {
        return b.<EmptyStatementTreeImpl>nonterminal(ApexLexer.EMPTY_STATEMENT)
                .is(f.emptyStatement(b.token(ApexPunctuator.SEMICOLON)));
    }

    public ExpressionTree EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.EXPRESSION)
                .is(CONDITIONAL_EXPRESSION());
    }

    public ExpressionTree CONDITIONAL_EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.CONDITIONAL_EXPRESSION)
                .is(
                        f.completeTernaryExpression(
                                CONDITIONAL_OR_EXPRESSION(),
                                b.optional(
                                        f.newTernaryExpression(
                                                b.token(ApexPunctuator.QUERY),
                                                EXPRESSION(),
                                                b.token(ApexPunctuator.COLON),
                                                EXPRESSION()))));
    }

    public ExpressionTree CONDITIONAL_OR_EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.CONDITIONAL_OR_EXPRESSION)
                .is(
                        f.binaryExpression10(
                                CONDITIONAL_AND_EXPRESSION(),
                                b.zeroOrMore(
                                        f.newOperatorAndOperand10(
                                                b.token(ApexPunctuator.OROR),
                                                CONDITIONAL_AND_EXPRESSION()))));
    }

    public ExpressionTree CONDITIONAL_AND_EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.CONDITIONAL_AND_EXPRESSION)
                .is(
                        f.binaryExpression9(
                                EQUALITY_EXPRESSION(),
                                b.zeroOrMore(
                                        f.newOperatorAndOperand9(
                                                b.token(ApexPunctuator.ANDAND),
                                                EQUALITY_EXPRESSION()))));
    }

    public ExpressionTree EQUALITY_EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.EQUALITY_EXPRESSION)
                .is(
                        f.binaryExpression5(
                                PRIMARY_EXPRESSION(),
                                b.zeroOrMore(
                                        f.newOperatorAndOperand5(
                                                b.firstOf(
                                                        b.token(ApexPunctuator.EQUAL),
                                                        b.token(ApexPunctuator.NOTEQUAL)),
                                                PRIMARY_EXPRESSION()))));
    }

    //TODO: Fix this according apex grammar
    public ExpressionTree PRIMARY_EXPRESSION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.PRIMARY_EXPRESSION)
                .is(
                        f.applySelectors1(
                                PRIMARY_PREFIX(),
                                b.zeroOrMore(
                                        PRIMARY_SUFFIX()
                                ))
                );
    }

    public ExpressionTree PRIMARY_PREFIX() {
        return b.<ExpressionTree>nonterminal(ApexLexer.PRIMARY_PREFIX)
                .is(
                        IDENTIFIER_OR_METHOD_INVOCATION());
    }

    public TreeFactory.Tuple<Optional<InternalSyntaxToken>, ExpressionTree> PRIMARY_SUFFIX() {
        return b.<TreeFactory.Tuple<Optional<InternalSyntaxToken>, ExpressionTree>>nonterminal(ApexLexer.PRIMARY_SUFFIX)
                .is(
                        f.completeMemberSelectOrMethodSelector(b.token(ApexPunctuator.DOT), IDENTIFIER_OR_METHOD_INVOCATION()));
    }

    public ExpressionTree IDENTIFIER_OR_METHOD_INVOCATION() {
        return b.<ExpressionTree>nonterminal(ApexLexer.IDENTIFIER_OR_METHOD_INVOCATION)
                .is(
                        f.newIdentifierOrMethodInvocation(
                                b.firstOf(
                                        b.token(ApexLexer.IDENTIFIER),
                                        b.token(ApexKeyword.THIS),
                                        b.token(ApexKeyword.SUPER)),
                                b.optional(ARGUMENTS())));
    }

    public ArgumentListTreeImpl ARGUMENTS() {
        return b.<ArgumentListTreeImpl>nonterminal(ApexLexer.ARGUMENTS)
                .is(
                        f.completeArguments(
                                b.token(ApexPunctuator.LPAREN),
                                b.optional(
                                        f.newArguments(
                                                EXPRESSION(),
                                                b.zeroOrMore(f.newTuple20(b.token(ApexPunctuator.COMMA), EXPRESSION())))),
                                b.token(ApexPunctuator.RPAREN)));
    }
//endregion Expression

    //region Formal Parameters
    public FormalParametersListTreeImpl FORMAL_PARAMETERS() {
        return b.<FormalParametersListTreeImpl>nonterminal(ApexLexer.FORMAL_PARAMETERS)
                .is(
                        f.completeParenFormalParameters(
                                b.token(ApexPunctuator.LPAREN),
                                b.optional(FORMAL_PARAMETERS_DECLS()),
                                b.token(ApexPunctuator.RPAREN)));
    }

    public FormalParametersListTreeImpl FORMAL_PARAMETERS_DECLS() {
        return b.<FormalParametersListTreeImpl>nonterminal(ApexLexer.FORMAL_PARAMETER)
                .is(
                        f.completeFormalParametersDecls(
                                b.optional(b.token(ApexKeyword.FINAL)),
                                TYPE(),
                                FORMAL_PARAMETERS_DECLS_REST()));
    }

    public FormalParametersListTreeImpl FORMAL_PARAMETERS_DECLS_REST() {
        return b.<FormalParametersListTreeImpl>nonterminal(ApexLexer.FORMAL_PARAMETERS_DECLS_REST)
                .is(
                        f.prependNewFormalParameter(
                                VARIABLE_DECLARATOR_ID(),
                                b.optional(f.newTuple18(b.token(ApexPunctuator.COMMA), FORMAL_PARAMETERS_DECLS()))));
    }

    public VariableTreeImpl VARIABLE_DECLARATOR_ID() {
        return b.<VariableTreeImpl>nonterminal(ApexLexer.VARIABLE_DECLARATOR_ID)
                .is(
                        f.newVariableDeclaratorId(
                                b.token(ApexLexer.IDENTIFIER)));
    }
//endregion formal parameters

    public TypeTree TYPE() {
        return b.<TypeTree>nonterminal(ApexLexer.TYPE)
                .is(
                        f.newType(
                                b.firstOf(
                                        b.token(ApexLexer.ALLOWED_KEYWORDS_AS_IDENTIFIER),
                                        b.token(ApexLexer.SPECIAL_KEYWORDS_AS_IDENTIFIER)),
                                b.zeroOrMore(
                                        f.newTuple1(
                                                b.token(ApexPunctuator.DOT),
                                                b.firstOf(
                                                        b.token(ApexLexer.ALLOWED_KEYWORDS_AS_IDENTIFIER),
                                                        b.token(ApexLexer.SPECIAL_KEYWORDS_AS_IDENTIFIER))))));
    }

}
