/* ============================================
   HELP DESK PI IV — Logica de Registro
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const registerError = document.getElementById('registerError');
    const btnRegister = document.getElementById('btnRegister');
    const departmentSelect = document.getElementById('regDepartment');
    DEPARTMENTS.forEach(department => departmentSelect.add(new Option(department, department)));

    // API URL BASE
    const API_URL = 'https://help-desk-pi-iv.onrender.com';

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const name = document.getElementById('regName').value.trim();
        const email = document.getElementById('regEmail').value.trim();
        const jobTitle = document.getElementById('regJobTitle').value.trim();
        const department = document.getElementById('regDepartment').value.trim();
        const password = document.getElementById('regPassword').value;
        const passwordConfirm = document.getElementById('regPasswordConfirm').value;

        registerError.style.display = 'none';

        if (!DEPARTMENTS.includes(department)) {
            registerError.textContent = 'Selecione um setor válido.';
            registerError.style.display = 'block';
            return;
        }

        // Validacao de senhas
        if (password !== passwordConfirm) {
            registerError.textContent = 'As senhas nao coincidem.';
            registerError.style.display = 'block';
            return;
        }

        btnRegister.disabled = true;
        btnRegister.textContent = 'Criando conta...';

        // Novos usuarios cadastrados pelo formulario publico entram como 'usuario' (CLIENT).
        // Futuramente o administrador do sistema podera promover
        // usuarios para 'tecnico' (SUPPORT) ou 'admin' (ADMIN)
        // atraves do painel de gerenciamento de permissoes.
        const role = 'usuario';

        try {
            const response = await fetch(`${API_URL}/api/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, jobTitle, department, password, role })
            });

            const data = await response.json();

            if (response.ok) {
                // Redireciona para o login com parametro de sucesso
                window.location.href = 'login.html?registered=true';
            } else {
                registerError.textContent = data.error || 'Erro ao criar conta. Tente novamente.';
                registerError.style.display = 'block';
            }
        } catch (error) {
            console.error('Register error:', error);
            registerError.textContent = 'Erro de conexao com o servidor. Tente novamente mais tarde.';
            registerError.style.display = 'block';
        } finally {
            btnRegister.disabled = false;
            btnRegister.textContent = 'Criar Conta';
        }
    });
});
