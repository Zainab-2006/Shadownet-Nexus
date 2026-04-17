describe('Operator Flow', () => {
  it('shows the operator selection route and backend roster', () => {
    cy.visit('/operators');
    cy.contains('Choose Your').should('be.visible');
    cy.contains('Operator').should('be.visible');
    cy.contains('Enter Dossier').should('exist');
  });
});
