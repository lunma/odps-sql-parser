// Generated from cn\cic\dataplatform\sql\parser\autogen\OdpsParser.g4 by ANTLR 4.9.2
package com.sea.odps.sql.autogen;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OdpsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OdpsParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OdpsParser#script}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScript(OdpsParser.ScriptContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#userCodeBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserCodeBlock(OdpsParser.UserCodeBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(OdpsParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(OdpsParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(OdpsParser.EmptyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#execStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecStatement(OdpsParser.ExecStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#cteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteStatement(OdpsParser.CteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableAliasWithCols}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAliasWithCols(OdpsParser.TableAliasWithColsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#subQuerySource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubQuerySource(OdpsParser.SubQuerySourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#explainStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainStatement(OdpsParser.ExplainStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(OdpsParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement(OdpsParser.LoopStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(OdpsParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionParameters(OdpsParser.FunctionParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#parameterDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDefinition(OdpsParser.ParameterDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(OdpsParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#parameterTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeDeclaration(OdpsParser.ParameterTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeDeclaration(OdpsParser.FunctionTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#parameterTypeDeclarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeDeclarationList(OdpsParser.ParameterTypeDeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#parameterColumnNameTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterColumnNameTypeList(OdpsParser.ParameterColumnNameTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#parameterColumnNameType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterColumnNameType(OdpsParser.ParameterColumnNameTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#varSizeParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarSizeParam(OdpsParser.VarSizeParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#assignStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStatement(OdpsParser.AssignStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#preSelectClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreSelectClauses(OdpsParser.PreSelectClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#postSelectClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostSelectClauses(OdpsParser.PostSelectClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectRest(OdpsParser.SelectRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multiInsertFromRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertFromRest(OdpsParser.MultiInsertFromRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fromRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromRest(OdpsParser.FromRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#simpleQueryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleQueryExpression(OdpsParser.SimpleQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectQueryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectQueryExpression(OdpsParser.SelectQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fromQueryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromQueryExpression(OdpsParser.FromQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setOperationFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetOperationFactor(OdpsParser.SetOperationFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#queryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpression(OdpsParser.QueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#queryExpressionWithCTE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpressionWithCTE(OdpsParser.QueryExpressionWithCTEContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setRHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRHS(OdpsParser.SetRHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multiInsertSetOperationFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertSetOperationFactor(OdpsParser.MultiInsertSetOperationFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multiInsertSelect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertSelect(OdpsParser.MultiInsertSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multiInsertSetRHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertSetRHS(OdpsParser.MultiInsertSetRHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multiInsertBranch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertBranch(OdpsParser.MultiInsertBranchContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fromStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStatement(OdpsParser.FromStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatement(OdpsParser.InsertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectQueryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectQueryStatement(OdpsParser.SelectQueryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#queryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryStatement(OdpsParser.QueryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#insertStatementWithCTE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatementWithCTE(OdpsParser.InsertStatementWithCTEContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#subQueryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubQueryExpression(OdpsParser.SubQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(OdpsParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fromSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromSource(OdpsParser.FromSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableVariableSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableVariableSource(OdpsParser.TableVariableSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableFunctionSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFunctionSource(OdpsParser.TableFunctionSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createMachineLearningModelStatment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMachineLearningModelStatment(OdpsParser.CreateMachineLearningModelStatmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#variableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableName(OdpsParser.VariableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#atomExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpression(OdpsParser.AtomExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#variableRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableRef(OdpsParser.VariableRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#variableCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableCall(OdpsParser.VariableCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#funNameRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunNameRef(OdpsParser.FunNameRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(OdpsParser.LambdaExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lambdaParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaParameter(OdpsParser.LambdaParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableOrColumnRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOrColumnRef(OdpsParser.TableOrColumnRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#newExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpression(OdpsParser.NewExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#existsExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistsExpression(OdpsParser.ExistsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#scalarSubQueryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarSubQueryExpression(OdpsParser.ScalarSubQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#classNameWithPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassNameWithPackage(OdpsParser.ClassNameWithPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#classNameOrArrayDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassNameOrArrayDecl(OdpsParser.ClassNameOrArrayDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#classNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassNameList(OdpsParser.ClassNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#odpsqlNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOdpsqlNonReserved(OdpsParser.OdpsqlNonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#relaxedKeywords}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelaxedKeywords(OdpsParser.RelaxedKeywordsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#allIdentifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllIdentifiers(OdpsParser.AllIdentifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(OdpsParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#aliasIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasIdentifier(OdpsParser.AliasIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#identifierWithoutSql11}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierWithoutSql11(OdpsParser.IdentifierWithoutSql11Context ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTableChangeOwner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableChangeOwner(OdpsParser.AlterTableChangeOwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterViewChangeOwner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewChangeOwner(OdpsParser.AlterViewChangeOwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTableEnableHubTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableEnableHubTable(OdpsParser.AlterTableEnableHubTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableLifecycle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLifecycle(OdpsParser.TableLifecycleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStatement(OdpsParser.SetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#anythingButEqualOrSemi}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnythingButEqualOrSemi(OdpsParser.AnythingButEqualOrSemiContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#anythingButSemi}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnythingButSemi(OdpsParser.AnythingButSemiContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setProjectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetProjectStatement(OdpsParser.SetProjectStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(OdpsParser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewInfoVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewInfoVal(OdpsParser.SkewInfoValContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#memberAccessOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberAccessOperator(OdpsParser.MemberAccessOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#methodAccessOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodAccessOperator(OdpsParser.MethodAccessOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#isNullOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNullOperator(OdpsParser.IsNullOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#inOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInOperator(OdpsParser.InOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#betweenOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBetweenOperator(OdpsParser.BetweenOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpression(OdpsParser.MathExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#unarySuffixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnarySuffixExpression(OdpsParser.UnarySuffixExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#unaryPrefixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryPrefixExpression(OdpsParser.UnaryPrefixExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fieldExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldExpression(OdpsParser.FieldExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpression(OdpsParser.LogicalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#notExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(OdpsParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#equalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualExpression(OdpsParser.EqualExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mathExpressionListInParentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionListInParentheses(OdpsParser.MathExpressionListInParenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mathExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionList(OdpsParser.MathExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(OdpsParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#statisticStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatisticStatement(OdpsParser.StatisticStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addRemoveStatisticStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddRemoveStatisticStatement(OdpsParser.AddRemoveStatisticStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#statisticInfo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatisticInfo(OdpsParser.StatisticInfoContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showStatisticStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStatisticStatement(OdpsParser.ShowStatisticStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showStatisticListStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStatisticListStatement(OdpsParser.ShowStatisticListStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#countTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountTableStatement(OdpsParser.CountTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#statisticName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatisticName(OdpsParser.StatisticNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#instanceManagement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceManagement(OdpsParser.InstanceManagementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#instanceStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceStatus(OdpsParser.InstanceStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#killInstance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKillInstance(OdpsParser.KillInstanceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#instanceId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceId(OdpsParser.InstanceIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#resourceManagement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceManagement(OdpsParser.ResourceManagementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addResource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddResource(OdpsParser.AddResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropResource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropResource(OdpsParser.DropResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#resourceId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceId(OdpsParser.ResourceIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropOfflineModel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropOfflineModel(OdpsParser.DropOfflineModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#getResource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGetResource(OdpsParser.GetResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptions(OdpsParser.OptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#authorizationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuthorizationStatement(OdpsParser.AuthorizationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listUsers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListUsers(OdpsParser.ListUsersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listGroups}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListGroups(OdpsParser.ListGroupsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addUserStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddUserStatement(OdpsParser.AddUserStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addGroupStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddGroupStatement(OdpsParser.AddGroupStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeUserStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveUserStatement(OdpsParser.RemoveUserStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeGroupStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveGroupStatement(OdpsParser.RemoveGroupStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addAccountProvider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddAccountProvider(OdpsParser.AddAccountProviderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeAccountProvider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveAccountProvider(OdpsParser.RemoveAccountProviderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showAcl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowAcl(OdpsParser.ShowAclContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listRoles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListRoles(OdpsParser.ListRolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#whoami}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhoami(OdpsParser.WhoamiContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listTrustedProjects}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListTrustedProjects(OdpsParser.ListTrustedProjectsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addTrustedProject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddTrustedProject(OdpsParser.AddTrustedProjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeTrustedProject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveTrustedProject(OdpsParser.RemoveTrustedProjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showSecurityConfiguration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowSecurityConfiguration(OdpsParser.ShowSecurityConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showPackages}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowPackages(OdpsParser.ShowPackagesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowItems(OdpsParser.ShowItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#installPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstallPackage(OdpsParser.InstallPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#uninstallPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUninstallPackage(OdpsParser.UninstallPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatePackage(OdpsParser.CreatePackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#deletePackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeletePackage(OdpsParser.DeletePackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addToPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddToPackage(OdpsParser.AddToPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeFromPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveFromPackage(OdpsParser.RemoveFromPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#allowPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllowPackage(OdpsParser.AllowPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#disallowPackage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisallowPackage(OdpsParser.DisallowPackageContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#putPolicy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPutPolicy(OdpsParser.PutPolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#getPolicy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGetPolicy(OdpsParser.GetPolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#clearExpiredGrants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClearExpiredGrants(OdpsParser.ClearExpiredGrantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#grantLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantLabel(OdpsParser.GrantLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#revokeLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokeLabel(OdpsParser.RevokeLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowLabel(OdpsParser.ShowLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#grantSuperPrivilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantSuperPrivilege(OdpsParser.GrantSuperPrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#revokeSuperPrivilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokeSuperPrivilege(OdpsParser.RevokeSuperPrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createRoleStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateRoleStatement(OdpsParser.CreateRoleStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropRoleStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropRoleStatement(OdpsParser.DropRoleStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#addRoleToProject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddRoleToProject(OdpsParser.AddRoleToProjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#removeRoleFromProject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveRoleFromProject(OdpsParser.RemoveRoleFromProjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#grantRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantRole(OdpsParser.GrantRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#revokeRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokeRole(OdpsParser.RevokeRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#grantPrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantPrivileges(OdpsParser.GrantPrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegeProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeProperties(OdpsParser.PrivilegePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegePropertieKeys}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegePropertieKeys(OdpsParser.PrivilegePropertieKeysContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#revokePrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokePrivileges(OdpsParser.RevokePrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#purgePrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPurgePrivileges(OdpsParser.PurgePrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showGrants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowGrants(OdpsParser.ShowGrantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showRoleGrants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowRoleGrants(OdpsParser.ShowRoleGrantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showRoles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowRoles(OdpsParser.ShowRolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showRolePrincipals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowRolePrincipals(OdpsParser.ShowRolePrincipalsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#user}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser(OdpsParser.UserContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#userRoleComments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserRoleComments(OdpsParser.UserRoleCommentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#accountProvider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccountProvider(OdpsParser.AccountProviderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#projectName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjectName(OdpsParser.ProjectNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegeObjectName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeObjectName(OdpsParser.PrivilegeObjectNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegeObjectType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeObjectType(OdpsParser.PrivilegeObjectTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#roleName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleName(OdpsParser.RoleNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(OdpsParser.PackageNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#packageNameWithProject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageNameWithProject(OdpsParser.PackageNameWithProjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#principalSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrincipalSpecification(OdpsParser.PrincipalSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#principalName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrincipalName(OdpsParser.PrincipalNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#principalIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrincipalIdentifier(OdpsParser.PrincipalIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege(OdpsParser.PrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegeType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeType(OdpsParser.PrivilegeTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#privilegeObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeObject(OdpsParser.PrivilegeObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#filePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilePath(OdpsParser.FilePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#policyCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyCondition(OdpsParser.PolicyConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#policyConditionOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyConditionOp(OdpsParser.PolicyConditionOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#policyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyKey(OdpsParser.PolicyKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#policyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyValue(OdpsParser.PolicyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showCurrentRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCurrentRole(OdpsParser.ShowCurrentRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRole(OdpsParser.SetRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#adminOptionFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdminOptionFor(OdpsParser.AdminOptionForContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#withAdminOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithAdminOption(OdpsParser.WithAdminOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#withGrantOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithGrantOption(OdpsParser.WithGrantOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#grantOptionFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantOptionFor(OdpsParser.GrantOptionForContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#explainOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainOption(OdpsParser.ExplainOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#loadStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadStatement(OdpsParser.LoadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#replicationClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplicationClause(OdpsParser.ReplicationClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#exportStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExportStatement(OdpsParser.ExportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#importStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportStatement(OdpsParser.ImportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#readStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadStatement(OdpsParser.ReadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#undoStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUndoStatement(OdpsParser.UndoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#redoStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRedoStatement(OdpsParser.RedoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#purgeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPurgeStatement(OdpsParser.PurgeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropTableVairableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableVairableStatement(OdpsParser.DropTableVairableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#msckRepairTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsckRepairTableStatement(OdpsParser.MsckRepairTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#ddlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStatement(OdpsParser.DdlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionSpecOrPartitionId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpecOrPartitionId(OdpsParser.PartitionSpecOrPartitionIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableOrTableId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOrTableId(OdpsParser.TableOrTableIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableHistoryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableHistoryStatement(OdpsParser.TableHistoryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setExstore}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetExstore(OdpsParser.SetExstoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#ifExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExists(OdpsParser.IfExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#restrictOrCascade}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestrictOrCascade(OdpsParser.RestrictOrCascadeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#ifNotExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfNotExists(OdpsParser.IfNotExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#rewriteEnabled}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRewriteEnabled(OdpsParser.RewriteEnabledContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#rewriteDisabled}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRewriteDisabled(OdpsParser.RewriteDisabledContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#storedAsDirs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoredAsDirs(OdpsParser.StoredAsDirsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#orReplace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrReplace(OdpsParser.OrReplaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#ignoreProtection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoreProtection(OdpsParser.IgnoreProtectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createDatabaseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDatabaseStatement(OdpsParser.CreateDatabaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(OdpsParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createSchemaStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSchemaStatement(OdpsParser.CreateSchemaStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dbLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbLocation(OdpsParser.DbLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dbProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbProperties(OdpsParser.DbPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dbPropertiesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbPropertiesList(OdpsParser.DbPropertiesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#switchDatabaseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchDatabaseStatement(OdpsParser.SwitchDatabaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropDatabaseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropDatabaseStatement(OdpsParser.DropDatabaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropSchemaStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropSchemaStatement(OdpsParser.DropSchemaStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#databaseComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseComment(OdpsParser.DatabaseCommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dataFormatDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataFormatDesc(OdpsParser.DataFormatDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableStatement(OdpsParser.CreateTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#truncateTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncateTableStatement(OdpsParser.TruncateTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndexStatement(OdpsParser.CreateIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#indexComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexComment(OdpsParser.IndexCommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#autoRebuild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAutoRebuild(OdpsParser.AutoRebuildContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#indexTblName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexTblName(OdpsParser.IndexTblNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#indexPropertiesPrefixed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexPropertiesPrefixed(OdpsParser.IndexPropertiesPrefixedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#indexProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexProperties(OdpsParser.IndexPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#indexPropertiesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexPropertiesList(OdpsParser.IndexPropertiesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndexStatement(OdpsParser.DropIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableStatement(OdpsParser.DropTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatement(OdpsParser.AlterStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterSchemaStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSchemaStatementSuffix(OdpsParser.AlterSchemaStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTableStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableStatementSuffix(OdpsParser.AlterTableStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTableMergePartitionSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableMergePartitionSuffix(OdpsParser.AlterTableMergePartitionSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixAddConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixAddConstraint(OdpsParser.AlterStatementSuffixAddConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTblPartitionStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTblPartitionStatementSuffix(OdpsParser.AlterTblPartitionStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixPartitionLifecycle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixPartitionLifecycle(OdpsParser.AlterStatementSuffixPartitionLifecycleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTblPartitionStatementSuffixProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTblPartitionStatementSuffixProperties(OdpsParser.AlterTblPartitionStatementSuffixPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementPartitionKeyType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementPartitionKeyType(OdpsParser.AlterStatementPartitionKeyTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterViewStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewStatementSuffix(OdpsParser.AlterViewStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterMaterializedViewStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMaterializedViewStatementSuffix(OdpsParser.AlterMaterializedViewStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterMaterializedViewSuffixRewrite}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMaterializedViewSuffixRewrite(OdpsParser.AlterMaterializedViewSuffixRewriteContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterMaterializedViewSuffixRebuild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMaterializedViewSuffixRebuild(OdpsParser.AlterMaterializedViewSuffixRebuildContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterIndexStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndexStatementSuffix(OdpsParser.AlterIndexStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterDatabaseStatementSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabaseStatementSuffix(OdpsParser.AlterDatabaseStatementSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterDatabaseSuffixProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabaseSuffixProperties(OdpsParser.AlterDatabaseSuffixPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterDatabaseSuffixSetOwner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabaseSuffixSetOwner(OdpsParser.AlterDatabaseSuffixSetOwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixRename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixRename(OdpsParser.AlterStatementSuffixRenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixAddCol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixAddCol(OdpsParser.AlterStatementSuffixAddColContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixRenameCol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixRenameCol(OdpsParser.AlterStatementSuffixRenameColContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixDropCol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixDropCol(OdpsParser.AlterStatementSuffixDropColContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixUpdateStatsCol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixUpdateStatsCol(OdpsParser.AlterStatementSuffixUpdateStatsColContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementChangeColPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementChangeColPosition(OdpsParser.AlterStatementChangeColPositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixAddPartitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixAddPartitions(OdpsParser.AlterStatementSuffixAddPartitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixAddPartitionsElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixAddPartitionsElement(OdpsParser.AlterStatementSuffixAddPartitionsElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixTouch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixTouch(OdpsParser.AlterStatementSuffixTouchContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixArchive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixArchive(OdpsParser.AlterStatementSuffixArchiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixUnArchive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixUnArchive(OdpsParser.AlterStatementSuffixUnArchiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixChangeOwner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixChangeOwner(OdpsParser.AlterStatementSuffixChangeOwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionLocation(OdpsParser.PartitionLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixDropPartitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixDropPartitions(OdpsParser.AlterStatementSuffixDropPartitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixProperties(OdpsParser.AlterStatementSuffixPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterViewSuffixProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewSuffixProperties(OdpsParser.AlterViewSuffixPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterViewColumnCommentSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewColumnCommentSuffix(OdpsParser.AlterViewColumnCommentSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixSerdeProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixSerdeProperties(OdpsParser.AlterStatementSuffixSerdePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tablePartitionPrefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePartitionPrefix(OdpsParser.TablePartitionPrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixFileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixFileFormat(OdpsParser.AlterStatementSuffixFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixClusterbySortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixClusterbySortby(OdpsParser.AlterStatementSuffixClusterbySortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterTblPartitionStatementSuffixSkewedLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTblPartitionStatementSuffixSkewedLocation(OdpsParser.AlterTblPartitionStatementSuffixSkewedLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedLocations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedLocations(OdpsParser.SkewedLocationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedLocationsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedLocationsList(OdpsParser.SkewedLocationsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedLocationMap}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedLocationMap(OdpsParser.SkewedLocationMapContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixLocation(OdpsParser.AlterStatementSuffixLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixSkewedby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixSkewedby(OdpsParser.AlterStatementSuffixSkewedbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixExchangePartition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixExchangePartition(OdpsParser.AlterStatementSuffixExchangePartitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixProtectMode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixProtectMode(OdpsParser.AlterStatementSuffixProtectModeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixRenamePart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixRenamePart(OdpsParser.AlterStatementSuffixRenamePartContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixStatsPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixStatsPart(OdpsParser.AlterStatementSuffixStatsPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixMergeFiles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixMergeFiles(OdpsParser.AlterStatementSuffixMergeFilesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterProtectMode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterProtectMode(OdpsParser.AlterProtectModeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterProtectModeMode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterProtectModeMode(OdpsParser.AlterProtectModeModeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixBucketNum}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixBucketNum(OdpsParser.AlterStatementSuffixBucketNumContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#alterStatementSuffixCompact}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatementSuffixCompact(OdpsParser.AlterStatementSuffixCompactContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileFormat(OdpsParser.FileFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tabTypeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTabTypeExpr(OdpsParser.TabTypeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partTypeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartTypeExpr(OdpsParser.PartTypeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#descStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescStatement(OdpsParser.DescStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#analyzeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyzeStatement(OdpsParser.AnalyzeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#forColumnsStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForColumnsStatement(OdpsParser.ForColumnsStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrList(OdpsParser.ColumnNameOrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStatement(OdpsParser.ShowStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListStatement(OdpsParser.ListStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#bareDate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBareDate(OdpsParser.BareDateContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockStatement(OdpsParser.LockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lockDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockDatabase(OdpsParser.LockDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lockMode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockMode(OdpsParser.LockModeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#unlockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlockStatement(OdpsParser.UnlockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#unlockDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlockDatabase(OdpsParser.UnlockDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#resourceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceList(OdpsParser.ResourceListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(OdpsParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#resourceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceType(OdpsParser.ResourceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createFunctionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFunctionStatement(OdpsParser.CreateFunctionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropFunctionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropFunctionStatement(OdpsParser.DropFunctionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#reloadFunctionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloadFunctionStatement(OdpsParser.ReloadFunctionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createMacroStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMacroStatement(OdpsParser.CreateMacroStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropMacroStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropMacroStatement(OdpsParser.DropMacroStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createSqlFunctionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSqlFunctionStatement(OdpsParser.CreateSqlFunctionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#cloneTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneTableStatement(OdpsParser.CloneTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createViewStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateViewStatement(OdpsParser.CreateViewStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#viewPartition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewPartition(OdpsParser.ViewPartitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropViewStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropViewStatement(OdpsParser.DropViewStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#createMaterializedViewStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMaterializedViewStatement(OdpsParser.CreateMaterializedViewStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropMaterializedViewStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropMaterializedViewStatement(OdpsParser.DropMaterializedViewStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showFunctionIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFunctionIdentifier(OdpsParser.ShowFunctionIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#showStmtIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStmtIdentifier(OdpsParser.ShowStmtIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableComment(OdpsParser.TableCommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tablePartition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePartition(OdpsParser.TablePartitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableBuckets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableBuckets(OdpsParser.TableBucketsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableShards}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableShards(OdpsParser.TableShardsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableSkewed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSkewed(OdpsParser.TableSkewedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#rowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormat(OdpsParser.RowFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#recordReader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordReader(OdpsParser.RecordReaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#recordWriter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordWriter(OdpsParser.RecordWriterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#rowFormatSerde}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatSerde(OdpsParser.RowFormatSerdeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#rowFormatDelimited}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatDelimited(OdpsParser.RowFormatDelimitedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowFormat(OdpsParser.TableRowFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tablePropertiesPrefixed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePropertiesPrefixed(OdpsParser.TablePropertiesPrefixedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableProperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableProperties(OdpsParser.TablePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tablePropertiesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePropertiesList(OdpsParser.TablePropertiesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#keyValueProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyValueProperty(OdpsParser.KeyValuePropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#userDefinedJoinPropertiesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserDefinedJoinPropertiesList(OdpsParser.UserDefinedJoinPropertiesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#keyPrivProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyPrivProperty(OdpsParser.KeyPrivPropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#keyProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyProperty(OdpsParser.KeyPropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowFormatFieldIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowFormatFieldIdentifier(OdpsParser.TableRowFormatFieldIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowFormatCollItemsIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowFormatCollItemsIdentifier(OdpsParser.TableRowFormatCollItemsIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowFormatMapKeysIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowFormatMapKeysIdentifier(OdpsParser.TableRowFormatMapKeysIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowFormatLinesIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowFormatLinesIdentifier(OdpsParser.TableRowFormatLinesIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableRowNullFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRowNullFormat(OdpsParser.TableRowNullFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableFileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFileFormat(OdpsParser.TableFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLocation(OdpsParser.TableLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#externalTableResource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternalTableResource(OdpsParser.ExternalTableResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#viewResource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewResource(OdpsParser.ViewResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#outOfLineConstraints}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutOfLineConstraints(OdpsParser.OutOfLineConstraintsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#enableSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnableSpec(OdpsParser.EnableSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#validateSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValidateSpec(OdpsParser.ValidateSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#relySpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelySpec(OdpsParser.RelySpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameTypeConstraintList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameTypeConstraintList(OdpsParser.ColumnNameTypeConstraintListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameTypeList(OdpsParser.ColumnNameTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionColumnNameTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionColumnNameTypeList(OdpsParser.PartitionColumnNameTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameTypeConstraintWithPosList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameTypeConstraintWithPosList(OdpsParser.ColumnNameTypeConstraintWithPosListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameColonTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameColonTypeList(OdpsParser.ColumnNameColonTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameList(OdpsParser.ColumnNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameListInParentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameListInParentheses(OdpsParser.ColumnNameListInParenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(OdpsParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrderList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrderList(OdpsParser.ColumnNameOrderListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#clusterColumnNameOrderList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClusterColumnNameOrderList(OdpsParser.ClusterColumnNameOrderListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedValueElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedValueElement(OdpsParser.SkewedValueElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedColumnValuePairList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedColumnValuePairList(OdpsParser.SkewedColumnValuePairListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedColumnValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedColumnValuePair(OdpsParser.SkewedColumnValuePairContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedColumnValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedColumnValues(OdpsParser.SkewedColumnValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedColumnValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedColumnValue(OdpsParser.SkewedColumnValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewedValueLocationElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewedValueLocationElement(OdpsParser.SkewedValueLocationElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrder(OdpsParser.ColumnNameOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameCommentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameCommentList(OdpsParser.ColumnNameCommentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameComment(OdpsParser.ColumnNameCommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnRefOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnRefOrder(OdpsParser.ColumnRefOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameTypeConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameTypeConstraint(OdpsParser.ColumnNameTypeConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameType(OdpsParser.ColumnNameTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionColumnNameType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionColumnNameType(OdpsParser.PartitionColumnNameTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifier(OdpsParser.MultipartIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameTypeConstraintWithPos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameTypeConstraintWithPos(OdpsParser.ColumnNameTypeConstraintWithPosContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#constraints}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraints(OdpsParser.ConstraintsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#primaryKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKey(OdpsParser.PrimaryKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#nullableSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullableSpec(OdpsParser.NullableSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(OdpsParser.DefaultValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameColonType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameColonType(OdpsParser.ColumnNameColonTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#colType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColType(OdpsParser.ColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#colTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColTypeList(OdpsParser.ColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#anyType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyType(OdpsParser.AnyTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#anyTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyTypeList(OdpsParser.AnyTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableTypeInfo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableTypeInfo(OdpsParser.TableTypeInfoContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(OdpsParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(OdpsParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#builtinTypeOrUdt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltinTypeOrUdt(OdpsParser.BuiltinTypeOrUdtContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#primitiveTypeOrUdt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveTypeOrUdt(OdpsParser.PrimitiveTypeOrUdtContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#listType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListType(OdpsParser.ListTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#structType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructType(OdpsParser.StructTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mapType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapType(OdpsParser.MapTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#unionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionType(OdpsParser.UnionTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetOperator(OdpsParser.SetOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#withClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause(OdpsParser.WithClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#insertClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertClause(OdpsParser.InsertClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#destination}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestination(OdpsParser.DestinationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(OdpsParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnAssignmentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnAssignmentClause(OdpsParser.ColumnAssignmentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#setColumnsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetColumnsClause(OdpsParser.SetColumnsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStatement(OdpsParser.UpdateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeStatement(OdpsParser.MergeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeTargetTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeTargetTable(OdpsParser.MergeTargetTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeSourceTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeSourceTable(OdpsParser.MergeSourceTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeAction(OdpsParser.MergeActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeValuesCaluse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeValuesCaluse(OdpsParser.MergeValuesCaluseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeSetColumnsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeSetColumnsClause(OdpsParser.MergeSetColumnsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mergeColumnAssignmentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeColumnAssignmentClause(OdpsParser.MergeColumnAssignmentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(OdpsParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectList(OdpsParser.SelectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectTrfmClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectTrfmClause(OdpsParser.SelectTrfmClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintClause(OdpsParser.HintClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintList(OdpsParser.HintListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintItem(OdpsParser.HintItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dynamicfilterHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDynamicfilterHint(OdpsParser.DynamicfilterHintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#mapJoinHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapJoinHint(OdpsParser.MapJoinHintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewJoinHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewJoinHint(OdpsParser.SkewJoinHintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectivityHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectivityHint(OdpsParser.SelectivityHintContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#multipleSkewHintArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleSkewHintArgs(OdpsParser.MultipleSkewHintArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewJoinHintArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewJoinHintArgs(OdpsParser.SkewJoinHintArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewColumns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewColumns(OdpsParser.SkewColumnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#skewJoinHintKeyValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewJoinHintKeyValues(OdpsParser.SkewJoinHintKeyValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintName(OdpsParser.HintNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintArgs(OdpsParser.HintArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#hintArgName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintArgName(OdpsParser.HintArgNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItem(OdpsParser.SelectItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#trfmClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrfmClause(OdpsParser.TrfmClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpression(OdpsParser.SelectExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#selectExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpressionList(OdpsParser.SelectExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_clause(OdpsParser.Window_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_defn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_defn(OdpsParser.Window_defnContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_specification(OdpsParser.Window_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_frame}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_frame(OdpsParser.Window_frameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#frame_exclusion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_exclusion(OdpsParser.Frame_exclusionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_frame_start_boundary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_frame_start_boundary(OdpsParser.Window_frame_start_boundaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#window_frame_boundary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_frame_boundary(OdpsParser.Window_frame_boundaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableAllColumns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAllColumns(OdpsParser.TableAllColumnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableOrColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOrColumn(OdpsParser.TableOrColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableAndColumnRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAndColumnRef(OdpsParser.TableAndColumnRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(OdpsParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#aliasList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasList(OdpsParser.AliasListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(OdpsParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#joinSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinSource(OdpsParser.JoinSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#joinRHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinRHS(OdpsParser.JoinRHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#uniqueJoinSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUniqueJoinSource(OdpsParser.UniqueJoinSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#uniqueJoinExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUniqueJoinExpr(OdpsParser.UniqueJoinExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#uniqueJoinToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUniqueJoinToken(OdpsParser.UniqueJoinTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#joinToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinToken(OdpsParser.JoinTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#lateralView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateralView(OdpsParser.LateralViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAlias(OdpsParser.TableAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableBucketSample}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableBucketSample(OdpsParser.TableBucketSampleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#splitSample}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSplitSample(OdpsParser.SplitSampleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableSample}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSample(OdpsParser.TableSampleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSource(OdpsParser.TableSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#availableSql11KeywordsForOdpsTableAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAvailableSql11KeywordsForOdpsTableAlias(OdpsParser.AvailableSql11KeywordsForOdpsTableAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(OdpsParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitioningSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitioningSpec(OdpsParser.PartitioningSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionTableFunctionSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionTableFunctionSource(OdpsParser.PartitionTableFunctionSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionedTableFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionedTableFunction(OdpsParser.PartitionedTableFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(OdpsParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#valueRowConstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueRowConstructor(OdpsParser.ValueRowConstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#valuesTableConstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesTableConstructor(OdpsParser.ValuesTableConstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#valuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesClause(OdpsParser.ValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#virtualTableSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVirtualTableSource(OdpsParser.VirtualTableSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableNameColList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNameColList(OdpsParser.TableNameColListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionTypeCubeOrRollup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeCubeOrRollup(OdpsParser.FunctionTypeCubeOrRollupContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupingSetsItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetsItem(OdpsParser.GroupingSetsItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetsClause(OdpsParser.GroupingSetsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupByKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByKey(OdpsParser.GroupByKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(OdpsParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupingSetExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetExpression(OdpsParser.GroupingSetExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupingSetExpressionMultiple}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetExpressionMultiple(OdpsParser.GroupingSetExpressionMultipleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#groupingExpressionSingle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingExpressionSingle(OdpsParser.GroupingExpressionSingleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(OdpsParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#havingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingCondition(OdpsParser.HavingConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#expressionsInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionsInParenthese(OdpsParser.ExpressionsInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#expressionsNotInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionsNotInParenthese(OdpsParser.ExpressionsNotInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnRefOrderInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnRefOrderInParenthese(OdpsParser.ColumnRefOrderInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnRefOrderNotInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnRefOrderNotInParenthese(OdpsParser.ColumnRefOrderNotInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(OdpsParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrIndexInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrIndexInParenthese(OdpsParser.ColumnNameOrIndexInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrIndexNotInParenthese}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrIndexNotInParenthese(OdpsParser.ColumnNameOrIndexNotInParentheseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#columnNameOrIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameOrIndex(OdpsParser.ColumnNameOrIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#zorderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZorderByClause(OdpsParser.ZorderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#clusterByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClusterByClause(OdpsParser.ClusterByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionByClause(OdpsParser.PartitionByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#distributeByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistributeByClause(OdpsParser.DistributeByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#sortByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortByClause(OdpsParser.SortByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(OdpsParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgument(OdpsParser.FunctionArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#builtinFunctionStructure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltinFunctionStructure(OdpsParser.BuiltinFunctionStructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(OdpsParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(OdpsParser.CastExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(OdpsParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#whenExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenExpression(OdpsParser.WhenExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(OdpsParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#simpleStringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleStringLiteral(OdpsParser.SimpleStringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(OdpsParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#doubleQuoteStringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleQuoteStringLiteral(OdpsParser.DoubleQuoteStringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#charSetStringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharSetStringLiteral(OdpsParser.CharSetStringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dateLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateLiteral(OdpsParser.DateLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dateTimeLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeLiteral(OdpsParser.DateTimeLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#timestampLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampLiteral(OdpsParser.TimestampLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#intervalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalLiteral(OdpsParser.IntervalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#intervalQualifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalQualifiers(OdpsParser.IntervalQualifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#intervalQualifiersUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalQualifiersUnit(OdpsParser.IntervalQualifiersUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#intervalQualifierPrecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalQualifierPrecision(OdpsParser.IntervalQualifierPrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#booleanValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanValue(OdpsParser.BooleanValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#tableOrPartition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOrPartition(OdpsParser.TableOrPartitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpec(OdpsParser.PartitionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#partitionVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionVal(OdpsParser.PartitionValContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dateWithoutQuote}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateWithoutQuote(OdpsParser.DateWithoutQuoteContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#dropPartitionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropPartitionSpec(OdpsParser.DropPartitionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#sysFuncNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSysFuncNames(OdpsParser.SysFuncNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#descFuncNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescFuncNames(OdpsParser.DescFuncNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#functionIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionIdentifier(OdpsParser.FunctionIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#reserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReserved(OdpsParser.ReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#nonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReserved(OdpsParser.NonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#sql11ReservedKeywordsUsedAsCastFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql11ReservedKeywordsUsedAsCastFunctionName(OdpsParser.Sql11ReservedKeywordsUsedAsCastFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdpsParser#sql11ReservedKeywordsUsedAsIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql11ReservedKeywordsUsedAsIdentifier(OdpsParser.Sql11ReservedKeywordsUsedAsIdentifierContext ctx);
}